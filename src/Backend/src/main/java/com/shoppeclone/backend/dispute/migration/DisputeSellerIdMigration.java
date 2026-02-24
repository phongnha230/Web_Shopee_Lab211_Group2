package com.shoppeclone.backend.dispute.migration;

import com.shoppeclone.backend.dispute.entity.Dispute;
import com.shoppeclone.backend.dispute.repository.DisputeRepository;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DisputeSellerIdMigration implements ApplicationRunner {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== [Migration] Backfill dispute.sellerId from order.shopId ===");

        List<Dispute> disputes = disputeRepository.findAll();
        int updated = 0;
        int skippedNoOrder = 0;
        int skippedNoShop = 0;

        for (Dispute dispute : disputes) {
            if (dispute == null) {
                continue;
            }

            String currentSellerId = dispute.getSellerId();
            if (currentSellerId != null && !currentSellerId.trim().isEmpty()) {
                continue; // already has sellerId
            }

            String orderId = dispute.getOrderId();
            if (orderId == null || orderId.trim().isEmpty()) {
                skippedNoOrder++;
                continue;
            }

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                skippedNoOrder++;
                continue;
            }

            String shopId = order.getShopId();
            if (shopId == null || shopId.trim().isEmpty()) {
                skippedNoShop++;
                continue;
            }

            dispute.setSellerId(shopId.trim());
            disputeRepository.save(dispute);
            updated++;

            log.info("  [OK] dispute {} <- sellerId {} (order {})", dispute.getId(), shopId, orderId);
        }

        log.info("=== [Migration] Done. total={}, updated={}, noOrder={}, noShopId={} ===",
                disputes.size(), updated, skippedNoOrder, skippedNoShop);
    }
}

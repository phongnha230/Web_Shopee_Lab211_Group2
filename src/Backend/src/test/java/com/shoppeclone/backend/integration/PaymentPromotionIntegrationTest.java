package com.shoppeclone.backend.integration;

import com.shoppeclone.backend.payment.entity.PaymentMethod;
import com.shoppeclone.backend.payment.repository.PaymentMethodRepository;
import com.shoppeclone.backend.payment.repository.PaymentRepository;
import com.shoppeclone.backend.promotion.entity.Voucher;
import com.shoppeclone.backend.promotion.repository.VoucherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentPromotionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentMethodRepository paymentMethodRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private VoucherRepository voucherRepository;

    @MockBean
    private com.shoppeclone.backend.promotion.repository.ShopVoucherRepository shopVoucherRepository;

    @MockBean
    private com.shoppeclone.backend.promotion.repository.FlashSaleRepository flashSaleRepository;

    @MockBean
    private com.shoppeclone.backend.promotion.repository.FlashSaleItemRepository flashSaleItemRepository;

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" }) // Simulate logged-in user
    public void testGetPaymentMethods() throws Exception {
        PaymentMethod pm1 = new PaymentMethod();
        pm1.setId("1");
        pm1.setCode("COD");
        pm1.setName("Cash On Delivery");

        PaymentMethod pm2 = new PaymentMethod();
        pm2.setId("2");
        pm2.setCode("BANKING");
        pm2.setName("Bank Transfer");

        when(paymentMethodRepository.findAll()).thenReturn(Arrays.asList(pm1, pm2));

        mockMvc.perform(get("/api/payments/methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("COD"));
    }

    @Test
    @WithMockUser
    public void testCreateVoucher() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setCode("TEST10");
        voucher.setDiscountValue(new BigDecimal("10000"));
        voucher.setDiscountType(Voucher.DiscountType.FIXED_AMOUNT);
        voucher.setQuantity(100);

        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        mockMvc.perform(post("/api/vouchers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voucher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST10"));
    }

    @Test
    @WithMockUser
    public void testGetAllVouchers() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setCode("TEST10");
        when(voucherRepository.findAll()).thenReturn(List.of(voucher));

        mockMvc.perform(get("/api/vouchers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("TEST10"));
    }
}

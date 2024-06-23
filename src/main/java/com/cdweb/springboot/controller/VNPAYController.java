package com.cdweb.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cdweb.springboot.dto.OrderDTO;
import com.cdweb.springboot.dto.OrderItemDTO;
import com.cdweb.springboot.dto.OrderMapper;
import com.cdweb.springboot.entities.Order;
import com.cdweb.springboot.service.OrderService;
import com.cdweb.springboot.service.VNPAYService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class VNPAYController {
	@Autowired
	private VNPAYService vnPayService;
	@Autowired
	private OrderService orderService;

	@GetMapping("/create-order")
	public Map<String, String> createOrder(@RequestParam("orderTotal") int orderTotal,
			@RequestParam("orderInfor") String orderInfor, HttpServletRequest request) {
//        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		String baseUrl = "http://localhost:3000/payment-result";
		System.out.println("baseUrl:  " + baseUrl);
//		String vnpayUrl = vnPayService.createOrder(request,userId, customerName,customerEmail, customerMobile,
//				shippingAddress,orderItems,orderTotal, orderInfo, baseUrl);
		System.out.println("orderInfo:  " + orderInfor);
		String vnpayUrl = vnPayService.createOrder(request, orderTotal, orderInfor, baseUrl);
		System.out.println("orderInfo:  " + orderInfor);
		Map<String, String> response = new HashMap<>();
		response.put("paymentUrl", vnpayUrl);
		return response;
	}

	@GetMapping("/vnpay-payment-return")
	public Map<String, Object> paymentCompleted(HttpServletRequest request) {
		System.out.println("return chayj");
		int paymentStatus = vnPayService.orderReturn(request);

		String orderInfo = request.getParameter("vnp_OrderInfo");
        System.out.println("orderInfo = " + orderInfo);
		String paymentTime = request.getParameter("vnp_PayDate");
		String transactionId = request.getParameter("vnp_TransactionNo");
		String totalPrice = request.getParameter("vnp_Amount");

		Map<String, Object> response = new HashMap<>();
		response.put("orderId", orderInfo);
		response.put("totalPrice", totalPrice);
		response.put("paymentTime", paymentTime);
		response.put("transactionId", transactionId);
		response.put("paymentStatus", paymentStatus == 1 ? "success" : "fail");

		if (paymentStatus == 1) {
			 // Tách phần orderInfo và orderItems
	        String[] parts = orderInfo.split(" orderItems:");

	        // Tách các thông tin trong phần orderInfo
	        String[] infoParts = parts[0].split(";", 5); // Tách thành 5 phần

	        // Gán các giá trị vào các biến tương ứng
	        Long userId = Long.parseLong(infoParts[0].trim());
	        String customerName = infoParts[1].trim();
	        String customerEmail = infoParts[2].trim();
	        String customerMobile = infoParts[3].trim();
	        String shippingAddress = infoParts[4].trim();

	        // In ra các giá trị đã tách để kiểm tra
//	        System.out.println("userId = " + userId);
//	        System.out.println("customerName = " + customerName);
//	        System.out.println("customerEmail = " + customerEmail);
//	        System.out.println("customerMobile = " + customerMobile);
	        System.out.println("shippingAddress = " + shippingAddress);
//	        System.out.println("orderItems = " + orderItems);

	        // Gán giá trị của orderItems
	        String orderItems = parts[1].trim();
			List<OrderItemDTO> dtos = new ArrayList<OrderItemDTO>();
			String[] items = orderItems.split(";");
			for (int i = 0; i < items.length; i++) {
				String[] it = items[i].split(",");
				String productId = it[0];
				String quantity = it[1];
				dtos.add(new OrderItemDTO(Long.parseLong(productId), Integer.parseInt(quantity)));
			}
			OrderDTO orderDTO = new OrderDTO();
			orderDTO.setUserId(userId);
			orderDTO.setCustomerName(customerName);
			orderDTO.setCustomerEmail(customerEmail);
			orderDTO.setCustomerMobile(customerMobile);
			orderDTO.setShippingAddress(shippingAddress);
			orderDTO.setTotalPrice(Integer.parseInt(totalPrice));
			orderDTO.setPaymentTime(paymentTime);
			orderDTO.setTransactionId(Integer.parseInt(transactionId));
			orderDTO.setPaymentStatus(paymentStatus);
			orderDTO.setOrderStatus(1);
			orderDTO.setOrderItems(dtos);
			Order order = OrderMapper.toEntity(orderDTO);
			orderService.saveOrder(order);
		}

		return response;
	}
}

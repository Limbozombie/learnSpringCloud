package com.atguigu.springcloud.controller;

import com.atguigu.springcloud.entities.CommonResult;
import com.atguigu.springcloud.entities.Payment;
import com.atguigu.springcloud.lb.LoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.List;

@RestController
@Slf4j
public class OrderController {

	//	    public static final String PAYMENT_URL = "http://localhost:8001";
	public static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";

	@Resource
	private RestTemplate restTemplate;

	@Resource
	private LoadBalancer loadBalancer;

	@Resource
	private DiscoveryClient discoveryClient;

	@GetMapping("/consumer/payment/create")
	public CommonResult<Payment> create(@RequestBody Payment payment) {
		return restTemplate.postForObject(PAYMENT_URL + "/payment/create", payment, CommonResult.class);
	}

	@GetMapping("/consumer/payment/getForEntity/{id}")
	public CommonResult<Payment> getPayment2(@PathVariable("id") Long id) {
		ResponseEntity<CommonResult> entity = restTemplate.getForEntity(PAYMENT_URL + "/payment/get/" + id, CommonResult.class);

		if (entity.getStatusCode().is2xxSuccessful()) {
			return entity.getBody();
		} else {
			return new CommonResult<>(444, "操作失败！");
		}
	}

	@GetMapping("/consumer/payment/lb")
	public String getPaymentLB() {
		// 通过容器中的 discoveryClient和服务名来获取服务集群
		List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
		if (instances == null || instances.size() <= 0) {
			return null;
		}
		// 传入服务集群来计算出获取具体的服务实例
		ServiceInstance serviceInstance = loadBalancer.instances(instances);
		URI uri = serviceInstance.getUri();
		String url = uri + "/payment/lb";
		return restTemplate.getForObject(url, String.class);
	}
}

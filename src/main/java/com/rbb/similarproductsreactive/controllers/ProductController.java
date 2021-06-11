package com.rbb.similarproductsreactive.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.rbb.similarproducts.openapi.api.ProductApi;
import com.rbb.similarproducts.openapi.model.ProductDetail;
import com.rbb.similarproductsreactive.services.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ProductController implements ProductApi {

	@Autowired
	private ProductService productService;

	@Override
	public Mono<ResponseEntity<Flux<ProductDetail>>> getProductSimilar(final String productId,
			final ServerWebExchange exchange) {
		return Mono.just(this.productService.getProductSimilar(productId)).map(ResponseEntity::ok);
	}

}

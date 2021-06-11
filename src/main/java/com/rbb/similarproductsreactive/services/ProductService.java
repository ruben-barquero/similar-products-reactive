package com.rbb.similarproductsreactive.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rbb.similarproducts.openapi.model.ProductDetail;
import com.rbb.similarproductsreactive.mappers.ProductDetailMapper;
import com.rbb.similarproductsreactive.repositories.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	public Flux<ProductDetail> getProductSimilar(final String productId) {
		return this.productRepository.getSimilarProductIds(productId).parallel().runOn(Schedulers.parallel())
				.flatMap(this.productRepository::getProductDetail)
				.map(ProductDetailMapper.INSTANCE::productDetailEntityToProductDetail).sequential();
	}

}

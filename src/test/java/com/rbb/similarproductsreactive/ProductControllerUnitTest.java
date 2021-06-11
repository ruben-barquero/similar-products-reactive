package com.rbb.similarproductsreactive;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbb.similarproducts.openapi.model.ProductDetail;
import com.rbb.similarproductsreactive.controllers.ProductController;
import com.rbb.similarproductsreactive.exceptions.NotFoundException;
import com.rbb.similarproductsreactive.exceptions.ServiceException;
import com.rbb.similarproductsreactive.mappers.ProductDetailMapper;
import com.rbb.similarproductsreactive.models.ProductDetailEntity;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestConfig.class)
class ProductControllerUnitTest {

	@Autowired
	private ProductController productController;

	public static MockWebServer mockBackEnd;

	private ObjectMapper objectMapper = new ObjectMapper();

	@DynamicPropertySource
	static void properties(final DynamicPropertyRegistry dynamicPropertyRegistry) throws IOException {
		dynamicPropertyRegistry.add("existingApis.server.url",
				() -> String.format("http://localhost:%s", mockBackEnd.getPort()));
	}

	@BeforeAll
	static void setUp() throws IOException {
		mockBackEnd = new MockWebServer();
		mockBackEnd.start();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockBackEnd.shutdown();
	}

	@Test
	void getProductSimilarOk() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2), Integer.valueOf(3));

		final ProductDetailEntity productDetailEntity2 = new ProductDetailEntity();
		productDetailEntity2.setId("2");
		productDetailEntity2.setName("product2");
		productDetailEntity2.setPrice(BigDecimal.valueOf(2));
		productDetailEntity2.setAvailability(Boolean.TRUE);

		final ProductDetailEntity productDetailEntity3 = new ProductDetailEntity();
		productDetailEntity3.setId("3");
		productDetailEntity3.setName("product3");
		productDetailEntity3.setPrice(BigDecimal.valueOf(3));
		productDetailEntity3.setAvailability(Boolean.TRUE);

		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				try {
					final MockResponse mockResponse = new MockResponse();
					if (recordedRequest.getPath().endsWith("/product/1/similarids")) {
						mockResponse.setBody(objectMapper.writeValueAsString(similarProductIds))
								.addHeader("Content-Type", "application/json");
					} else if (recordedRequest.getPath().endsWith("/product/2")) {
						mockResponse.setBody(objectMapper.writeValueAsString(productDetailEntity2))
								.addHeader("Content-Type", "application/json");
					} else if (recordedRequest.getPath().endsWith("/product/3")) {
						mockResponse.setBody(objectMapper.writeValueAsString(productDetailEntity3))
								.addHeader("Content-Type", "application/json");
					} else {
						mockResponse.setResponseCode(404);
					}
					return mockResponse;
				} catch (JsonProcessingException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		});

		final Mono<ResponseEntity<Flux<ProductDetail>>> productSimilar = this.productController.getProductSimilar("1", null);

		final Set<ProductDetail> expectedResult = new LinkedHashSet<ProductDetail>(
				List.of(ProductDetailMapper.INSTANCE.productDetailEntityToProductDetail(productDetailEntity2),
						ProductDetailMapper.INSTANCE.productDetailEntityToProductDetail(productDetailEntity3)));

		Assert.assertEquals(expectedResult, productSimilar.block().getBody().collect(Collectors.toCollection(LinkedHashSet::new)).block());
	}

	@Test
	void getProductSimilarNotFoundSimilarProductIds() throws JsonProcessingException, URISyntaxException {
		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				return new MockResponse().setResponseCode(404);
			}
		});
		
		final Mono<ResponseEntity<Flux<ProductDetail>>> productSimilar = this.productController.getProductSimilar("1", null);
				
		final NotFoundException notFoundException = Assert.assertThrows(NotFoundException.class,
				() -> productSimilar.block().getBody().collect(Collectors.toCollection(LinkedHashSet::new)).block());
		
		Assert.assertEquals("No found similar products for the product 1", notFoundException.getMessage());
	}

	@Test
	void getProductSimilarNotFoundProductDetail() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2));
		
		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				try {
					final MockResponse mockResponse = new MockResponse();
					if (recordedRequest.getPath().endsWith("/product/1/similarids")) {
						mockResponse.setBody(objectMapper.writeValueAsString(similarProductIds))
								.addHeader("Content-Type", "application/json");
					} else {
						mockResponse.setResponseCode(404);
					}
					return mockResponse;
				} catch (JsonProcessingException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		});
		
		final Mono<ResponseEntity<Flux<ProductDetail>>> productSimilar = this.productController.getProductSimilar("1", null);
		
		final NotFoundException notFoundException = Assert.assertThrows(NotFoundException.class,
				() -> productSimilar.block().getBody().collect(Collectors.toCollection(LinkedHashSet::new)).block());

		Assert.assertEquals("Not found detail for the product 2", notFoundException.getMessage());
	}

	@Test
	void getProductSimilarErrorInSimilarProductIds() throws JsonProcessingException, URISyntaxException {
		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				return new MockResponse().setResponseCode(500);
			}
		});

		final Mono<ResponseEntity<Flux<ProductDetail>>> productSimilar = this.productController.getProductSimilar("1", null);
		
		final ServiceException serviceException = Assert.assertThrows(ServiceException.class,
				() -> productSimilar.block().getBody().collect(Collectors.toCollection(LinkedHashSet::new)).block());
		
		Assert.assertTrue(StringUtils.startsWith(serviceException.getMessage(), "An error occurred while retrieving similar products for the product 1."));
	}

	@Test
	void getProductSimilarErrorInProductDetail() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2));
		
		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				try {
					final MockResponse mockResponse = new MockResponse();
					if (recordedRequest.getPath().endsWith("/product/1/similarids")) {
						mockResponse.setBody(objectMapper.writeValueAsString(similarProductIds))
								.addHeader("Content-Type", "application/json");
					} else {
						mockResponse.setResponseCode(500);
					}
					return mockResponse;
				} catch (JsonProcessingException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		});

		final Mono<ResponseEntity<Flux<ProductDetail>>> productSimilar = this.productController.getProductSimilar("1", null);
		
		final ServiceException serviceException = Assert.assertThrows(ServiceException.class,
				() -> productSimilar.block().getBody().collect(Collectors.toCollection(LinkedHashSet::new)).block());
		
		Assert.assertTrue(StringUtils.startsWith(serviceException.getMessage(), "An error occurred while retrieving detail for the product 2."));
	}

}
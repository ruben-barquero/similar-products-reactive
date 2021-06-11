# SimilarProductsReactiveApi
Este proyecto es una de las soluciones (API reactiva) a la prueba técnica, donde se pide construir un API REST que devuelva el resultado combinado de la ejecución de dos API´s REST externas proporcionadas.

## Herramientas utilizadas
Se ha creado un **ApiREST** con **springboot**, **Java11**, **Lombok**, **MapStruct**, etc. Para la compilación y gestión de dependencias se ha utilizado **maven**.

La definición del Api se ha realizado mediante **Swagger**, haciendo uso de **OpenAPI** para la generación automática de código.
El archivo que define el Api ha sido proporcionado para realizar la prueba y se ha modificado para crear el esquema ProductDetail con el objeto resultado. Este archivo se encuentra ubicado en la siguiente ruta:
> src/main/resources/similarProducts.yaml

Además, se han realizado test de integración (**JUnit** y **MockWebServer**).

## Compile, Run & Test
Todas acciones se realizan a través **maven**.

Para compilar y ejecutar los test:
> mvn clean install

Los lanzar la aplicación:
> mvn spring-boot:run

## Endpoints
La definición del API puede verse [aquí](http://localhost:5000/swagger-ui/). Esta página permite explorar los diferentes recursos del api y así como realizar pruebas desde el navegador.

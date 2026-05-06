package com.microservicios.productos.controller;

import com.microservicios.productos.model.Producto;
import com.microservicios.productos.repository.ProductoRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/productos")
public class ProductosController {

    private static final Logger logger = LoggerFactory.getLogger(ProductosController.class);

    @Autowired
    private ProductoRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping
    public List<Producto> getProductos() {
        logger.info("Obteniendo todos los productos");
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Producto getProducto(@PathVariable String id) {
        logger.info("Obteniendo producto con id: " + id);
        return repository.findById(id).orElse(null);
    }

    @PostMapping
    public Producto crearProducto(@RequestBody Producto producto) {
        logger.info("Creando/actualizando producto: " + producto.getNombre());
        try {
            if (producto.getNombre() != null && producto.getNombre().contains("FAIL")) {
                throw new RuntimeException("Error simulado o falla real en procesamiento de producto");
            }
            return repository.save(producto);
        } catch (Exception e) {
            logger.error("Fallo al procesar el producto. Enviando a topico Kafka product_retry_jobs", e);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String payload = mapper.writeValueAsString(producto);
                kafkaTemplate.send("product_retry_jobs", payload);
            } catch (Exception ex) {
                logger.error("Fallo al enviar a Kafka", ex);
            }
            throw new RuntimeException("Error al procesar producto, guardado en fallidos.");
        }
    }

    @PutMapping("/{id}")
    public Producto actualizarProducto(@PathVariable String id, @RequestBody Producto producto) {
        logger.info("Actualizando producto con id: " + id);
        return repository.findById(id).map(prod -> {
            prod.setNombre(producto.getNombre());
            prod.setDescripcion(producto.getDescripcion());
            prod.setPrecio(producto.getPrecio());
            prod.setStock(producto.getStock());
            return repository.save(prod);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable String id) {
        logger.info("Eliminando producto con id: " + id);
        repository.deleteById(id);
    }
}

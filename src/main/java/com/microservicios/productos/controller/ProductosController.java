package com.microservicios.productos.controller;

import com.microservicios.productos.model.Producto;
import com.microservicios.productos.repository.ProductoRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/productos")
public class ProductosController {

    private static final Logger logger = LoggerFactory.getLogger(ProductosController.class);

    @Autowired
    private ProductoRepository repository;

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
        logger.info("Creando nuevo producto: " + producto.getNombre());
        return repository.save(producto);
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

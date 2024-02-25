package org.cibertec.edu.pe.controller;

import java.util.ArrayList;
import java.util.List;

import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.cibertec.edu.pe.model.Detalle;
import org.cibertec.edu.pe.model.Producto;
import org.cibertec.edu.pe.model.Venta;
import org.cibertec.edu.pe.repository.IDetalleRepository;
import org.cibertec.edu.pe.repository.IProductoRepository;
import org.cibertec.edu.pe.repository.IVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({"carrito", "total"})
public class ProductoController {
	@Autowired
	private IProductoRepository productoRepository;
	@Autowired
	private IVentaRepository ventaRepository;
	@Autowired
	private IDetalleRepository detalleRepository;
	
	@GetMapping("/index")
	public String listado(Model model) {
		List<Producto> lista = new ArrayList<>();
		lista = productoRepository.findAll();
		model.addAttribute("productos", lista);
		return "index";
	}
	

	@GetMapping("/agregar/{idProducto}")
    public String agregar(Model model, @PathVariable(name = "idProducto", required = true) int idProducto,
                          @ModelAttribute("carrito") List<Detalle> carrito,
                          @ModelAttribute("total") double total) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);

        if (producto != null) {
            boolean productoExistente = false;
            for (Detalle detalle : carrito) {
                if (detalle.getProducto().getIdProducto() == idProducto) {
                    detalle.setCantidad(detalle.getCantidad() + 1);
                    productoExistente = true;
                    break;
                }
            }

            if (!productoExistente) {
                Detalle nuevoDetalle = new Detalle();
                nuevoDetalle.setProducto(producto);
                nuevoDetalle.setCantidad(1);
                carrito.add(nuevoDetalle);
            }
        }
        return "redirect:/index";
    }
	
    @GetMapping("/carrito")
    public String carrito(Model model, @ModelAttribute("carrito") List<Detalle> carrito) {
        double total = calcularTotal(carrito);
        double precioEnvio = 5; 
        double descuento = total * 0.1; 
        double totalFinal = total + precioEnvio - descuento;
        
        model.addAttribute("subtotal", total);
        model.addAttribute("precioEnvio", precioEnvio);
        model.addAttribute("descuento", descuento);
        model.addAttribute("totalFinal", totalFinal);
        
        return "carrito";
    }
	
	@GetMapping("/pagar")
	public String realizarPago(@ModelAttribute("carrito") List<Detalle> carrito, Model model) {
	    // Guardar información de productos adquiridos en las tablas correspondientes
	    for (Detalle detalle : carrito) {
	        detalleRepository.save(detalle);
	    }
	    // Limpiar carrito después de realizar el pago
	    carrito.clear();
	    model.addAttribute("mensaje", "¡La compra se ha realizado con éxito!");
	    return "mensaje";
	}

	@PostMapping("/actualizarCarrito")
	public String actualizarCarrito(@ModelAttribute("carrito") List<Detalle> carrito, Model model) {
	    
	    return "redirect:/carrito";
	}

	
	// Inicializacion de variable de la sesion
	@ModelAttribute("carrito")
	public List<Detalle> getCarrito() {
		return new ArrayList<Detalle>();
	}
	
    private double calcularTotal(List<Detalle> carrito) {
        double total = 0.0;
        for (Detalle detalle : carrito) {
            detalle.setSubtotal(detalle.getProducto().getPrecio() * detalle.getCantidad());
            total += detalle.getSubtotal();
        }
        return total;
    }
	
	@ModelAttribute("total")
	public double getTotal() {
		return 0.0;
	}
}

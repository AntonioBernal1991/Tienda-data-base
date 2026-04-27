package model;

import java.sql.Date;

public class PedidoDetalleView {
    private int pedidoId;
    private int productoId;
    private Date fecha;
    private String cliente;
    private String producto;
    private int cantidad;
    private double precio;
    private double total;

    public PedidoDetalleView(int pedidoId, int productoId, Date fecha, String cliente, String producto, int cantidad, double precio, double total) {
        this.pedidoId = pedidoId;
        this.productoId = productoId;
        this.fecha = fecha;
        this.cliente = cliente;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.total = total;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public int getProductoId() {
        return productoId;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public double getTotal() {
        return total;
    }
}

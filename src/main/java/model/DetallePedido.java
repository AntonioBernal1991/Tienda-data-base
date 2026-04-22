package model;

public class DetallePedido {
    private int id;
    private int pedidoId;
    private int productoId;
    private int cantidad;

    public DetallePedido() {
    }

    public DetallePedido(int pedidoId, int productoId, int cantidad) {
        this.pedidoId = pedidoId;
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public int getProductoId() {
        return productoId;
    }

    public int getCantidad() {
        return cantidad;
    }
}
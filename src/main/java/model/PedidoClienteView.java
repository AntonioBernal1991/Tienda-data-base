package model;

import java.sql.Date;

public class PedidoClienteView {
    private final int pedidoId;
    private final Date fecha;
    private final String cliente;
    private final String ciudad;

    public PedidoClienteView(int pedidoId, Date fecha, String cliente, String ciudad) {
        this.pedidoId = pedidoId;
        this.fecha = fecha;
        this.cliente = cliente;
        this.ciudad = ciudad;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public String getCiudad() {
        return ciudad;
    }
}

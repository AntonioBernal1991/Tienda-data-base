package model;

public class Cliente {
    private int id;
    private String nombre;
    private String email;
    private String ciudad;

    public Cliente() {
    }

    public Cliente(String nombre, String email, String ciudad) {
        this.nombre = nombre;
        this.email = email;
        this.ciudad = ciudad;
    }

    public Cliente(int id, String nombre, String email, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.ciudad = ciudad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", ciudad='" + ciudad + '\'' +
                '}';
    }
}
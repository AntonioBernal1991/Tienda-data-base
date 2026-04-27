package org.example;

import dao.ClienteDAO;
import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
import model.Cliente;
import model.Producto;

import java.util.Scanner;

public class Main {
    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Número entero inválido. Inténtalo otra vez.");
            }
        }
    }

    private static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("Número decimal inválido. Usa coma o punto. Ej: 9,99 o 9.99");
            }
        }
    }

    private static boolean readYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sí") || s.equals("y") || s.equals("yes")) {
                return true;
            }
            if (s.equals("n") || s.equals("no")) {
                return false;
            }
            System.out.println("Respuesta no válida. Escribe 's' o 'n'.");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        ProductoDAO productoDAO = new ProductoDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        PedidoDAO pedidoDAO = new PedidoDAO();
        DetallePedidoDAO detalleDAO = new DetallePedidoDAO();

        int opcion;

        do {
            System.out.println("\n--- MENÚ TIENDA ---");
            System.out.println("1. Añadir producto");
            System.out.println("2. Listar productos");
            System.out.println("3. Actualizar producto");
            System.out.println("4. Eliminar producto");
            System.out.println("5. Añadir cliente");
            System.out.println("6. Listar clientes");
            System.out.println("7. Crear pedido");
            System.out.println("8. Ver pedidos con cliente");
            System.out.println("9. Ver detalle de pedidos");
            System.out.println("10. Salir");
            opcion = readInt(sc, "Elige una opción: ");

            switch (opcion) {
                case 1:
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();

                    double precio = readDouble(sc, "Precio: ");
                    int stock = readInt(sc, "Stock: ");

                    productoDAO.insertarProducto(new Producto(nombre, precio, stock));
                    break;

                case 2:
                    System.out.println("\n--- LISTA DE PRODUCTOS ---");
                    for (Producto p : productoDAO.listarProductos()) {
                        System.out.println(p);
                    }
                    break;

                case 3:
                    int idActualizar = readInt(sc, "ID del producto a actualizar: ");

                    System.out.print("Nuevo nombre: ");
                    String nuevoNombre = sc.nextLine();

                    double nuevoPrecio = readDouble(sc, "Nuevo precio: ");
                    int nuevoStock = readInt(sc, "Nuevo stock: ");

                    productoDAO.actualizarProducto(
                            new Producto(idActualizar, nuevoNombre, nuevoPrecio, nuevoStock)
                    );
                    break;

                case 4:
                    int idEliminar = readInt(sc, "ID del producto a eliminar: ");

                    productoDAO.eliminarProducto(idEliminar);
                    break;

                case 5:
                    System.out.print("Nombre del cliente: ");
                    String nombreCliente = sc.nextLine();
                    System.out.print("Email del cliente: ");
                    String email = sc.nextLine();
                    System.out.print("Ciudad del cliente: ");
                    String ciudad = sc.nextLine();

                    clienteDAO.insertarCliente(new Cliente(nombreCliente, email, ciudad));
                    break;

                case 6:
                    System.out.println("\n--- LISTA DE CLIENTES ---");
                    for (Cliente c : clienteDAO.listarClientes()) {
                        System.out.println(c);
                    }
                    break;

                case 7:
                    int clienteId = readInt(sc, "ID del cliente para crear el pedido: ");
                    int pedidoId = pedidoDAO.crearPedido(clienteId);

                    if (pedidoId <= 0) {
                        System.out.println("No se pudo crear el pedido, se cancela la adición de productos.");
                        break;
                    }

                    System.out.println("\n--- PRODUCTOS DISPONIBLES ---");
                    for (Producto p : productoDAO.listarProductos()) {
                        System.out.println(p);
                    }

                    boolean seguir = true;
                    while (seguir) {
                        int productoId = readInt(sc, "ID del producto a añadir al pedido: ");
                        int cantidad = readInt(sc, "Cantidad: ");

                        detalleDAO.agregarProductoAPedido(pedidoId, productoId, cantidad);

                        seguir = readYesNo(sc, "¿Añadir otro producto al pedido? (s/n): ");
                    }

                    System.out.println("Pedido " + pedidoId + " finalizado.");
                    break;

                case 8:
                    pedidoDAO.listarPedidosConCliente();
                    break;

                case 9:
                    detalleDAO.verDetallePedidos();
                    break;

                case 10:
                    System.out.println("Saliendo del programa...");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }

        } while (opcion != 10);

        sc.close();
    }
}

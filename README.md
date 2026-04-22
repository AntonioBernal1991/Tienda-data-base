# Tienda DB

Aplicación de escritorio en JavaFX para la gestión de una pequeña tienda. Permite administrar productos y clientes almacenados en una base de datos PostgreSQL.

## Características

### Productos
- Listar todos los productos desde la base de datos.
- Añadir nuevos productos (nombre, precio y stock).
- Actualizar productos existentes mediante su ID (actualización parcial: solo se cambian los campos rellenos).
- Eliminar productos por ID con confirmación previa.

### Clientes
- Listar todos los clientes.
- Añadir nuevos clientes (nombre y email, con validación básica de formato).

### Navegación
- Menú principal con acceso a las distintas vistas.
- Botón "Volver" en cada vista para regresar al menú.

## Tecnologías

- **Java 23**
- **JavaFX 23.0.2** (controles y FXML)
- **PostgreSQL 42.7.3** (driver JDBC)
- **Maven** (gestión de dependencias y compilación)

## Estructura del proyecto

```
src/main/
├── java/
│   ├── dao/                        Acceso a datos (JDBC)
│   │   ├── ConexionBD.java
│   │   ├── ClienteDAO.java
│   │   ├── ProductoDAO.java
│   │   ├── PedidoDAO.java
│   │   └── DetallePedidoDAO.java
│   ├── model/                      Clases entidad
│   │   ├── Cliente.java
│   │   ├── Producto.java
│   │   ├── Pedido.java
│   │   └── DetallePedido.java
│   └── org/example/
│       ├── App.java                Punto de entrada JavaFX
│       ├── database/
│       │   └── DataBaseConnection.java
│       └── ui/                     Controladores FXML
│           ├── MainController.java
│           ├── ProductController.java
│           └── ClientController.java
└── resources/org/example/ui/       Vistas FXML
    ├── MainView.fxml
    ├── ProductView.fxml
    └── ClientView.fxml
```

## Requisitos previos

- JDK 23 instalado.
- Maven 3.8 o superior.
- PostgreSQL 14 o superior con una base de datos creada (por defecto `Tienda_db`).

## Esquema de la base de datos

```sql
CREATE TABLE productos (
    id      SERIAL PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    precio  NUMERIC(10,2) NOT NULL,
    stock   INTEGER NOT NULL
);

CREATE TABLE clientes (
    id      SERIAL PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    email   VARCHAR(150) NOT NULL
);

CREATE TABLE pedidos (
    id          SERIAL PRIMARY KEY,
    cliente_id  INTEGER REFERENCES clientes(id)
);

CREATE TABLE detalle_pedido (
    id           SERIAL PRIMARY KEY,
    pedido_id    INTEGER REFERENCES pedidos(id),
    producto_id  INTEGER REFERENCES productos(id),
    cantidad     INTEGER NOT NULL
);
```

## Configuración

Antes de ejecutar la aplicación, ajusta los datos de conexión en:

`src/main/java/org/example/database/DataBaseConnection.java`

```java
private static final String URL = "jdbc:postgresql://localhost:5432/Tienda_db";
private static final String USER = "postgres";
private static final String PASSWORD = "tu_contraseña";
```

## Ejecución

Desde la raíz del proyecto:

```bash
mvn clean javafx:run
```

## Capturas

El menú principal muestra dos botones: **Productos** y **Clientes**. Cada vista tiene una tabla a la izquierda con el listado actual, un formulario a la derecha y botones de acción (Añadir, Actualizar, Eliminar, Listar según el caso). Un botón **Volver** en la esquina inferior derecha lleva de vuelta al menú.

## Autor

Antonio Bernal

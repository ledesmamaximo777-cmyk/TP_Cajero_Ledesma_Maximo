import java.util.ArrayList;

// Clase que representa la entidad del problema
class Producto {
    private String nombre;
    private double precio;
    private int stock;

    // Constructor
    public Producto(String nombre, double precio, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    // Getters y Setters básicos
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    
    public void setStock(int stock) { this.stock = stock; }

    // Método propio de la lógica de negocio
    public void aplicarDescuento(double porcentaje) {
        if (porcentaje > 0 && porcentaje <= 100) {
            this.precio -= this.precio * (porcentaje / 100);
        }
    }
}

// Clase principal con la lógica del "Examen"
public class GestionProductos {
    public static void main(String[] args) {
        // 1. Inicializar la estructura de datos
        ArrayList<Producto> inventario = new ArrayList<>();

        // 2. Carga de datos de prueba
        inventario.add(new Producto("Teclado Mecánico", 45000.0, 15));
        inventario.add(new Producto("Mouse Gamer", 18000.0, 3));
        inventario.add(new Producto("Monitor 24''", 120000.0, 0)); // Sin stock
        inventario.add(new Producto("Auriculares", 25000.0, 8));

        System.out.println("--- ESTADO INICIAL DEL INVENTARIO ---");
        for (Producto p : inventario) {
            System.out.println(p.getNombre() + " - Precio: $" + p.getPrecio() + " - Stock: " + p.getStock());
        }

        // 3. Resolución de consignas típicas de parcial
        
        // Consigna A: Calcular el valor total del inventario disponible
        double valorTotal = 0;
        for (Producto p : inventario) {
            valorTotal += p.getPrecio() * p.getStock();
        }
        System.out.println("\n> Valor total del inventario en stock: $" + valorTotal);

        // Consigna B: Mostrar los productos que tienen poco stock (menos de 5 unidades)
        System.out.println("\n> Alerta de reposición (Stock menor a 5 unidades):");
        for (Producto p : inventario) {
            if (p.getStock() < 5) {
                System.out.println("  - " + p.getNombre() + " (Quedan: " + p.getStock() + ")");
            }
        }

        // Consigna C: Aplicar un 10% de descuento solo a los productos que superen los $30,000
        System.out.println("\n> Aplicando 10% de descuento a productos VIP (Precio > $30.000)...");
        for (Producto p : inventario) {
            if (p.getPrecio() > 30000.0) {
                p.aplicarDescuento(10);
                System.out.println("  - Nuevo precio de " + p.getNombre() + ": $" + p.getPrecio());
            }
        }
    }
}

package Principal;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// ========================================================
// 1. EXCEPCIONES DE NEGOCIO (ALERTAS)
// ========================================================
class VehiculoNoEncontradoException extends RuntimeException {
    public VehiculoNoEncontradoException(String mensaje) { super(mensaje); }
}

class BateriaInsuficienteException extends RuntimeException {
    public BateriaInsuficienteException(String mensaje) { super(mensaje); }
}

// ========================================================
// 2. MODELO DE DOMINIO - USUARIOS
// ========================================================
abstract class Usuario {
    private String idUsuario;
    private String nombreCompleto;

    public Usuario(String idUsuario, String nombreCompleto) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
    }
    public abstract double aplicarDescuento(double costoBase);
    public String getIdUsuario() { return idUsuario; }
}

class UsuarioRegulares extends Usuario {
    public UsuarioRegulares(String idUsuario, String nombreCompleto) { super(idUsuario, nombreCompleto); }
    @Override
    public double aplicarDescuento(double costoBase) { return costoBase; }
}

class UsuarioPremium extends Usuario {
    private double porcentajeDescuento;
    public UsuarioPremium(String idUsuario, String nombreCompleto, double porcentajeDescuento) {
        super(idUsuario, nombreCompleto);
        this.porcentajeDescuento = porcentajeDescuento;
    }
    @Override
    public double aplicarDescuento(double costoBase) { return costoBase * (1.0 - porcentajeDescuento); }
}

// ========================================================
// 3. MODELO DE DOMINIO - VEHÍCULOS
// ========================================================
abstract class Vehiculo {
    private String patente;
    private int porcentajeBateria;
    private double tarifaFijaBase;

    public Vehiculo(String patente, int porcentajeBateria, double tarifaFijaBase) {
        this.patente = patente;
        this.porcentajeBateria = porcentajeBateria;
        this.tarifaFijaBase = tarifaFijaBase;
    }
    public abstract double calcularCostoFinal(Usuario usuario);
    public String getPatente() { return patente; }
    public int getPorcentajeBateria() { return porcentajeBateria; }
    public double getTarifaFijaBase() { return tarifaFijaBase; }
}

class Monopatin extends Vehiculo {
    private boolean sistemaAmortiguacionReforzada;
    public Monopatin(String patente, int porcentajeBateria, double tarifaFijaBase, boolean sistemaAmortiguacionReforzada) {
        super(patente, porcentajeBateria, tarifaFijaBase);
        this.sistemaAmortiguacionReforzada = sistemaAmortiguacionReforzada;
    }
    @Override
    public double calcularCostoFinal(Usuario usuario) { return usuario.aplicarDescuento(this.getTarifaFijaBase()); }
}

class BicicletaElectrica extends Vehiculo {
    private double capacidadCanastoCarga;
    public BicicletaElectrica(String patente, int porcentajeBateria, double tarifaFijaBase, double capacidadCanastoCarga) {
        super(patente, porcentajeBateria, tarifaFijaBase);
        this.capacidadCanastoCarga = capacidadCanastoCarga;
    }
    @Override
    public double calcularCostoFinal(Usuario usuario) { return usuario.aplicarDescuento(this.getTarifaFijaBase()); }
}

// ========================================================
// 4. ESTACIÓN DE ANCLAJE
// ========================================================
class Estacion {
    private String nombre;
    private List<Vehiculo> vehiculosDisponibles = new ArrayList<>();

    public Estacion(String nombre) { this.nombre = nombre; }
    
    public Vehiculo buscarVehiculoPorPatente(String patente) {
        for (Vehiculo v : vehiculosDisponibles) {
            if (v.getPatente().equalsIgnoreCase(patente)) { return v; }
        }
        throw new VehiculoNoEncontradoException("Vehiculo No Encontrado");
    }
    public void agregarVehiculo(Vehiculo v) { vehiculosDisponibles.add(v); }
    public void removerVehiculo(Vehiculo v) { vehiculosDisponibles.remove(v); }
}

// ========================================================
// 5. PROCESAMIENTO Y DESACOPLAMIENTO DE PAGOS (FACTORY)
// ========================================================
interface ProcesadorPago { void procesar(double monto); }

class ProcesadorTarjeta implements ProcesadorPago {
    public void procesar(double m) { System.out.printf("Cobro exitoso de $%.2f realizado con Tarjeta de Crédito%n", m); }
}
class ProcesadorBilletera implements ProcesadorPago {
    public void procesar(double m) { System.out.printf("Cobro exitoso de $%.2f realizado con Billetera Virtual%n", m); }
}

class FactoraProcesadorPago {
    public static ProcesadorPago obtenerProcesador(String medioPago) {
        if (medioPago.equalsIgnoreCase("TARJETA")) return new ProcesadorTarjeta();
        if (medioPago.equalsIgnoreCase("BILLETERA")) return new ProcesadorBilletera();
        throw new IllegalArgumentException("Medio de pago no soportado");
    }
}

// ========================================================
// 6. SERVICIO DE ALQUILER (LOGICA SECUENCIAL)
// ========================================================
class AlquilerService {
    private Map<String, Usuario> usuariosMock = new HashMap<>();
    private Estacion estacionMock = new Estacion("Central");

    public AlquilerService() {
        usuariosMock.put("USR123", new UsuarioPremium("USR123", "Juan Perez", 0.15));
        usuariosMock.put("USR456", new UsuarioRegulares("USR456", "Maria Gomez"));
        estacionMock.agregarVehiculo(new Monopatin("AAA111", 80, 500.0, true));
        estacionMock.agregarVehiculo(new BicicletaElectrica("BBB222", 10, 450.0, 1200.0));
    }

    public String procesarDesbloqueo(String idUsuario, String patente, String metodoPago) {
        Vehiculo v = estacionMock.buscarVehiculoPorPatente(patente);
        if (v.getPorcentajeBateria() < 15) throw new BateriaInsuficienteException("Batería Insuficiente");
        
        Usuario u = usuariosMock.get(idUsuario);
        if (u == null) throw new IllegalArgumentException("Usuario no registrado");
        
        double importe = v.calcularCostoFinal(u);
        
        ProcesadorPago p = FactoraProcesadorPago.obtenerProcesador(metodoPago);
        p.procesar(importe);
        estacionMock.removerVehiculo(v);
        
        return "Desbloqueo exitoso. Monto: $" + importe;
    }
}

// ========================================================
// 7. CLASE PRINCIPAL PARA EJECUTAR Y PROBAR
// ========================================================
public class Principal {
    public static void main(String[] args) {
        AlquilerService servicio = new AlquilerService();
        
        System.out.println("--- Probando Alquiler Exitoso ---");
        try {
            String resultado = servicio.procesarDesbloqueo("USR123", "AAA111", "TARJETA");
            System.out.println(resultado);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\n--- Probando Alerta: Bateria Insuficiente (<15%) ---");
        try {
            String resultado = servicio.procesarDesbloqueo("USR456", "BBB222", "BILLETERA");
            System.out.println(resultado);
        } catch (Exception e) {
            System.out.println("Alerta del sistema: " + e.getMessage());
        }
    }
}
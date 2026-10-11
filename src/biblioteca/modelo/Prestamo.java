package biblioteca.modelo;

import java.time.LocalDate;

public class Prestamo {
    public static final int DIAS_PRESTAMO = 7;

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    /** Vencimiento mientras el préstamo está pendiente; fecha real de devolución una vez devuelto. */
    private LocalDate fechaDevolucion;
    private boolean devuelto;
    private String tituloLibro;       // datos de lectura (JOIN)
    private String nombreEstudiante;

    public Prestamo() { }

    /** Crea un préstamo nuevo calculando automáticamente la fecha de vencimiento. */
    public static Prestamo nuevo(int idEstudiante, int idLibro) {
        Prestamo p = new Prestamo();
        p.idEstudiante = idEstudiante;
        p.idLibro = idLibro;
        p.fechaPrestamo = LocalDate.now();
        p.fechaDevolucion = p.fechaPrestamo.plusDays(DIAS_PRESTAMO);
        p.devuelto = false;
        return p;
    }

    public LocalDate getFechaVencimiento() {
        return fechaPrestamo.plusDays(DIAS_PRESTAMO);
    }

    /** Pendiente: vencido si hoy supera el vencimiento. Devuelto: atraso si se entregó después del vencimiento. */
    public boolean estaAtrasado() {
        LocalDate referencia = devuelto ? fechaDevolucion : LocalDate.now();
        return referencia.isAfter(getFechaVencimiento());
    }

    public long diasAtraso() {
        if (!estaAtrasado()) return 0;
        LocalDate referencia = devuelto ? fechaDevolucion : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(getFechaVencimiento(), referencia);
    }

    public String getEstado() {
        if (devuelto) return estaAtrasado() ? "Devuelto con atraso" : "Devuelto";
        return estaAtrasado() ? "ATRASADO" : "Vigente";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }
    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }
    public String getTituloLibro() { return tituloLibro; }
    public void setTituloLibro(String tituloLibro) { this.tituloLibro = tituloLibro; }
    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }
}

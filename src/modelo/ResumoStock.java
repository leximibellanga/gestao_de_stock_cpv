package modelo;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResumoStock {

    private int totalProdutos;
    private int totalUnidades;
    private double valorStock;
    private int abaixoMinimo;
    private int esgotados;
    private int inactivos;
    private int entradas7Dias;
    private int saidas7Dias;
    private Map<String, Integer> unidadesPorCategoria = new LinkedHashMap<>();

    public int getTotalProdutos() {
        return totalProdutos;
    }

    public void setTotalProdutos(int totalProdutos) {
        this.totalProdutos = totalProdutos;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public void setTotalUnidades(int totalUnidades) {
        this.totalUnidades = totalUnidades;
    }

    public double getValorStock() {
        return valorStock;
    }

    public void setValorStock(double valorStock) {
        this.valorStock = valorStock;
    }

    public int getAbaixoMinimo() {
        return abaixoMinimo;
    }

    public void setAbaixoMinimo(int abaixoMinimo) {
        this.abaixoMinimo = abaixoMinimo;
    }

    public int getEsgotados() {
        return esgotados;
    }

    public void setEsgotados(int esgotados) {
        this.esgotados = esgotados;
    }

    public int getInactivos() {
        return inactivos;
    }

    public void setInactivos(int inactivos) {
        this.inactivos = inactivos;
    }

    public int getEntradas7Dias() {
        return entradas7Dias;
    }

    public void setEntradas7Dias(int entradas7Dias) {
        this.entradas7Dias = entradas7Dias;
    }

    public int getSaidas7Dias() {
        return saidas7Dias;
    }

    public void setSaidas7Dias(int saidas7Dias) {
        this.saidas7Dias = saidas7Dias;
    }

    public Map<String, Integer> getUnidadesPorCategoria() {
        return unidadesPorCategoria;
    }

    public void setUnidadesPorCategoria(Map<String, Integer> m) {
        this.unidadesPorCategoria = m;
    }
}
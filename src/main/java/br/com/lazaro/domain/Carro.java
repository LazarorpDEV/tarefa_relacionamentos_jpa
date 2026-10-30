package br.com.lazaro.domain;

import jakarta.persistence.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "carro")
public class Carro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    // Uma marca pode ser utilizada por varios carros.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marca_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_carro_marca"))
    private Marca marca;

    // O mesmo acessorio pode ser compartilhado por varios carros.
    @ManyToMany
    @JoinTable(name = "carro_acessorio",
            joinColumns = @JoinColumn(name = "carro_id"),
            inverseJoinColumns = @JoinColumn(name = "acessorio_id"),
            foreignKey = @ForeignKey(name = "fk_ca_carro"),
            inverseForeignKey = @ForeignKey(name = "fk_ca_acessorio"),
            uniqueConstraints = @UniqueConstraint(name = "uk_carro_acessorio",
                    columnNames = {"carro_id", "acessorio_id"}))
    private Set<Acessorio> acessorios = new LinkedHashSet<>();

    protected Carro() { }

    public Carro(String modelo, Integer ano, Marca marca) {
        this.modelo = modelo;
        this.ano = ano;
        this.marca = Objects.requireNonNull(marca);
    }

    public void adicionarAcessorio(Acessorio acessorio) {
        acessorios.add(Objects.requireNonNull(acessorio));
    }

    public void removerAcessorio(Acessorio acessorio) { acessorios.remove(acessorio); }
    public Long getId() { return id; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Marca getMarca() { return marca; }
    public void setMarca(Marca marca) { this.marca = Objects.requireNonNull(marca); }
    public Set<Acessorio> getAcessorios() { return Collections.unmodifiableSet(acessorios); }
}

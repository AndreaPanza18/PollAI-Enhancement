package com.example.pollai.produzione;

import com.example.pollai.pollaio.Pollaio;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Column;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Entity
@Table(name = "produzione")
public class DatiProduzione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pollaio_id", nullable = false)
    private Pollaio pollaio;

    @ElementCollection
    @CollectionTable(name = "uova_per_data", joinColumns = @JoinColumn(name = "produzione_id"))
    @MapKeyColumn(name = "data")
    @Column(name = "uova")
    private Map<LocalDate, Integer> uovaPerGiorno = new HashMap<>();

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setPollaio(Pollaio pollaio){
        this.pollaio = pollaio;
    }

    public Pollaio getPollaio(){
        return pollaio;
    }

    public Map<LocalDate, Integer> getUovaPerGiorno() {
        return new TreeMap<>(uovaPerGiorno);
    }

    public void setUovaPerGiorno(Map<LocalDate, Integer> uovaPerGiorno) {
        this.uovaPerGiorno = uovaPerGiorno;
    }

    public void aggiungiUova(final LocalDate date, final Integer num){
        uovaPerGiorno.put(date, num);
    }

    public Integer cercaPerGiorno(final LocalDate date){
        final Integer uova = uovaPerGiorno.get(date);  // Get the value, it may be null
        if (uova == null) {
            return -1;  // Return -1 if the date is not found
        } else {
            return uova;  // Return the value if found
        }
    }
}

package com.example.pollai.produzione;

import com.example.pollai.pollaio.Pollaio;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Entity
@Table(name = "produzione")
//@ nullable_by_default
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
    private /*@ non_null @*/ Map<LocalDate, Integer> uovaPerGiorno = new HashMap<>();

    public void setId(Long id) {
        this.id = id;
    }

    public /*@ pure @*/ Long getId() {
        return id;
    }

    public void setPollaio(Pollaio pollaio){
        this.pollaio = pollaio;
    }

    public /*@ pure @*/ Pollaio getPollaio(){
        return pollaio;
    }

    public /*@ skipesc @*/ Map<LocalDate, Integer> getUovaPerGiorno() {
        return new TreeMap<>(uovaPerGiorno);
    }

    public void setUovaPerGiorno(/*@ non_null @*/ Map<LocalDate, Integer> uovaPerGiorno) {
        this.uovaPerGiorno = uovaPerGiorno;
    }

    public void aggiungiUova(/*@ non_null @*/ LocalDate date, /*@ non_null @*/ Integer num){
        uovaPerGiorno.put(date, num);
    }

    public /*@ pure @*/ Integer cercaPerGiorno(LocalDate date){
        Integer uova = uovaPerGiorno.get(date);  // Get the value, it may be null
        if (uova == null) {
            return -1;  // Return -1 if the date is not found
        } else {
            return uova;  // Return the value if found
        }
    }
}

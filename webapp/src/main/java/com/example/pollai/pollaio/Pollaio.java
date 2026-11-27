package com.example.pollai.pollaio;

import com.example.pollai.produzione.DatiProduzione;
import com.example.pollai.utente.Utente;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "pollaio")
//@ nullable_by_default
public class Pollaio implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    @OneToMany(mappedBy = "pollaio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List</*@ non_null @*/ Gallina> galline = new ArrayList<>();

    @OneToOne(mappedBy = "pollaio", cascade = CascadeType.ALL)
    private Utente utente;

    @OneToOne(mappedBy = "pollaio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DatiProduzione produzione;

    public Pollaio() {
    }

    public Pollaio(int quantity, /*@ nullable @*/ List</*@ non_null @*/ Gallina> galline, Utente utente) {
        List</*@ non_null @*/ Gallina> lista = galline != null ? galline : new ArrayList<>();
        this.galline = lista;
        this.quantity = lista.size();
        this.utente = utente;
    }

    public /*@ pure @*/ Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public /*@ pure @*/ int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public /*@ pure @*/ List</*@ non_null @*/ Gallina> getGalline() {
        //@ assume galline != null;
        return galline;
    }

    public /*@ pure @*/ Optional<Gallina> getGallinaByid(long id){
        //@ assume galline != null;
        for (Gallina g : galline){
            Long gid = g.getId();
            if (gid != null && gid.longValue() == id){
                return Optional.of(g);
            }
        }
        return Optional.empty();
    }

    public void setGalline(/*@ non_null @*/ List</*@ non_null @*/ Gallina> galline) {
        this.galline = galline;
    }

    public void addGallina(/*@ nullable @*/ Gallina gallina) {
        if (gallina != null) {
            List</*@ non_null @*/ Gallina> lista = galline;
            //@ assume lista != null;
            lista.add(gallina);
            gallina.setPollaio(this);
            quantity = lista.size();
        } else {
            throw new IllegalArgumentException("La gallina non può essere null");
        }
    }



    public void removeGallina(/*@ nullable @*/ Gallina gallina) {
        List</*@ non_null @*/ Gallina> lista = galline;
        //@ assume lista != null;
        if (gallina != null && lista.contains(gallina)) {
            lista.remove(gallina);
            gallina.setPollaio(null);
            quantity = lista.size();
        } else {
            System.out.println("Gallina non trovata nel pollaio");
        }
    }


    public /*@ pure @*/ Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public /*@ pure @*/ DatiProduzione getProduzione() {
        return produzione;
    }

    public void setProduzione(DatiProduzione produzione) {
        this.produzione = produzione;
    }


}

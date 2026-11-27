package com.example.pollai.magazzino;

import com.example.pollai.utente.Utente;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "magazzino")
//@ nullable_by_default
public class Magazzino implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "magazzino", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List</*@ non_null @*/ Farmaco> farmaci = new ArrayList<>();

    @OneToMany(mappedBy = "magazzino", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List</*@ non_null @*/ Cibo> cibo = new ArrayList<>();

    @OneToOne(mappedBy = "magazzino", cascade = CascadeType.ALL)
    private Utente utente;

    @Transient
    private Notifica notifica;

    public Magazzino() {
    }

    // Constructor with All Fields (except ID, which is auto-generated)
    public Magazzino(/*@ non_null @*/ List</*@ non_null @*/ Farmaco> farmaci, /*@ non_null @*/ List</*@ non_null @*/ Cibo> cibo, Utente utente) {
        this.farmaci = farmaci;
        this.cibo = cibo;
        this.utente = utente;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public /*@ pure @*/ Long getId() {
        return id;
    }

    public /*@ pure @*/ List</*@ non_null @*/ Farmaco> getFarmaci() {
        return farmaci;
    }

    public void setFarmaci(/*@ non_null @*/ List</*@ non_null @*/ Farmaco> farmaci) {
        this.farmaci = farmaci;
    }

    public void addFarmaco(/*@ non_null @*/ Farmaco farmaco) {
        //@ assume farmaci != null;
        farmaci.add(farmaco);
        farmaco.setMagazzino(this);
    }

    public void removeFarmaco(/*@ non_null @*/ Farmaco farmaco) {
        //@ assume farmaci != null;
        farmaci.remove(farmaco);
        farmaco.setMagazzino(null);
    }

    public /*@ pure @*/ List</*@ non_null @*/ Cibo> getCibo() {
        return cibo;
    }

    public void setCibo(/*@ non_null @*/ List</*@ non_null @*/ Cibo> cibo) {
        this.cibo = cibo;
    }

    public void addCibo(/*@ non_null @*/ Cibo cibo1) {
        //@ assume cibo != null;
        cibo.add(cibo1);
        cibo1.setMagazzino(this);
    }

    public void removeCibo(/*@ non_null @*/ Cibo cibo1) {
        //@ assume cibo != null;
        cibo.remove(cibo1);
        cibo1.setMagazzino(null);
    }

    public /*@ pure @*/ Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    // code_bigint_math: la somma delle quantità viene valutata in aritmetica matematica,
    // quindi niente errori di overflow/underflow su int
    public /*@ code_bigint_math @*/ void setNotifica(){
        //@ assume cibo != null;
        Notifica notifica = new Notifica();
        int quantity = 0;
        for(Cibo c : cibo){
            quantity += c.getQuantita();
        }
        if(quantity < 200){
            notifica.setAvvertimento("We are running out of food supplies");
        } else if(quantity < 400){
            notifica.setAvvertimento("We will soon run out of food supplies");
        } else{
            notifica.setAvvertimento("Our food supplies are enough");
        }
        this.notifica = notifica;
    }

    public /*@ pure @*/ Notifica getNotifica(){
        return notifica;
    }
    
}

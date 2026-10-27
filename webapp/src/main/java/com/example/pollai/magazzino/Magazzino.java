package com.example.pollai.magazzino;

import com.example.pollai.pollaio.Gallina;
import com.example.pollai.utente.Utente;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Transient;


import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "magazzino")
public class Magazzino implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "magazzino", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Farmaco> farmaci = new ArrayList<>();

    @OneToMany(mappedBy = "magazzino", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Cibo> cibo = new ArrayList<>();

    @OneToOne(mappedBy = "magazzino", cascade = CascadeType.ALL)
    private Utente utente;

    @Transient
    private Notifica notifica;

    public Magazzino() {
    }

    // Constructor with All Fields (except ID, which is auto-generated)
    public Magazzino(List<Farmaco> farmaci, List<Cibo> cibo, Utente utente) {
        this.farmaci = farmaci;
        this.cibo = cibo;
        this.utente = utente;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<Farmaco> getFarmaci() {
        return farmaci;
    }

    public void setFarmaci(List<Farmaco> farmaci) {
        this.farmaci = farmaci;
    }

    public void addFarmaco(final Farmaco farmaco) {
        farmaci.add(farmaco);
        farmaco.setMagazzino(this);
    }

    public void removeFarmaco(final Farmaco farmaco) {
        farmaci.remove(farmaco);
        farmaco.setMagazzino(null);
    }

    public List<Cibo> getCibo() {
        return cibo;
    }

    public void setCibo(List<Cibo> cibo) {
        this.cibo = cibo;
    }

    public void addCibo(final Cibo cibo1) {
        cibo.add(cibo1);
        cibo1.setMagazzino(this);
    }

    public void removeCibo(final Cibo cibo1) {
        cibo.remove(cibo1);
        cibo1.setMagazzino(null);
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public void setNotifica(){
        Notifica notifica = new Notifica();
        int quantity = 0;
        for(final Cibo c : cibo){
            quantity += c.getQuantita();
        }

        switch (quantity / 200) {
            case 0: // quantity da 0 a 199
                notifica.setAvvertimento("We are running out of food supplies");
                break;
            case 1: // quantity da 200 a 399
                notifica.setAvvertimento("We will soon run out of food supplies");
                break;
            default: // quantity >= 400
                notifica.setAvvertimento("Our food supplies are enough");
                break;
        }
        this.notifica = notifica;
    }

    public Notifica getNotifica(){
        return notifica;
    }
    
}

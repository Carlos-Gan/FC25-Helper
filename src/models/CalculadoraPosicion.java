package models;

public class CalculadoraPosicion {

    public static int calcularST(JugadorAtributos a) {
        return (a.finishing + a.shotPower + a.sprintSpeed + a.positioning) / 4;
    }

    public static int calcularCAM(JugadorAtributos a) {
        return (a.vision + a.shortPassing + a.ballControl + a.agility) / 4;
    }

    public static int calcularCB(JugadorAtributos a) {
        return (a.defensiveAwareness + a.standingTackle + a.strength + a.slidingTackle) / 4;
    }

    public static int calcularGK(JugadorAtributos a) {
        return (a.gkDiving + a.gkHandling + a.gkKicking + a.gkPositioning + a.gkReflexes) / 5;
    }
    

}

package com.example.zenword;

// Clase para guardar el estado del botón
public class EstadoBoton {
    private String text;
    private int textColor;
    private boolean isClickable;

    public EstadoBoton(String text, int textColor,boolean isClickable) {
        this.text = text;
        this.textColor = textColor;
        this.isClickable = isClickable;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }
}

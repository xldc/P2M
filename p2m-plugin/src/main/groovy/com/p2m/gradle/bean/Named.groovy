package com.p2m.gradle.bean


abstract class Named {
    protected String name
    public Named(String name) {
        if (name.startsWith(":")){
            this.name = name.substring(1)
        }else {
            this.name = name
        }
    }

    abstract String get()

    String getToLowerCase() {
        return get().toLowerCase()
    }

    @Override
    int hashCode() {
        return get().hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof Named) {
            return false
        }
        // println("${get()} equals ${o.get()}")
        return get().equals((o as Named).get())
    }
}


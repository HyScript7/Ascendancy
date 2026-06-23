package io.github.hyscript7.ascendancy.api.registry;

public interface Identifiable{
    /**
    * Must return the identifier for the object implementing this method
    */
    Identifier getIdentifier();
}
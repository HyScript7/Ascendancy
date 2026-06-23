package io.github.hyscript7.ascendancy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.github.hyscript7.ascendancy.api.registry.Reg;
import io.github.hyscript7.ascendancy.api.registry.RegIdentifiable;
import io.github.hyscript7.ascendancy.api.registry.RegIdentifier;
import io.github.hyscript7.ascendancy.api.registry.RegIdentifierCollision;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseReg<E extends RegIdentifiable> implements Reg<E>  {
    ConcurrentHashMap<RegIdentifier,E> storiage; 
    @Override
    public void register(E value) throws RegIdentifierCollision {
        if (storiage.get(value.getIdentifier()) == null) {
            storiage.put(value.getIdentifier(), value);
        }
        else
        {
            log.warn("An entry with an existing identidier was attempted to be made: " + value.getIdentifier());
            throw new RegIdentifierCollision(("An entry with the same identidier already exists: " + value.getIdentifier() ));
        }
    }

    @Override
    public List<E> getAll() {

        return(new ArrayList<>(storiage.values()));
    }

    @Override
    public Optional<E> get(RegIdentifier id) {
        Optional<E> entry = Optional.ofNullable(storiage.get(id));
        if (entry.isEmpty()){
            log.warn("Entity of an id \"" + id + "\" does not exist.");
        }
        return(entry);
    }
    
}

package com.ebicep.warlords.database.repositories.masterworksfair;

import com.ebicep.warlords.database.repositories.masterworksfair.pojos.MasterworksFair;
import org.springframework.stereotype.Service;

@Service
public interface MasterworksFairService {

    void create(MasterworksFair masterworksFair);

    void update(MasterworksFair masterworksFair);

    void delete(MasterworksFair masterworksFair);

    MasterworksFair findFirstByOrderByStartDateDesc();


}
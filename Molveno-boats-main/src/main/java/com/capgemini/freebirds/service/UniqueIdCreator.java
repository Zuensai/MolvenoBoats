package com.capgemini.freebirds.service;

import com.capgemini.freebirds.data.UniqueIdFileController;

public class UniqueIdCreator {



    public static int getNextUniqueId() {
        UniqueIdFileController file = new UniqueIdFileController();
        int nextUniqueId = file.getHighestUniqueId()+1;
        file.write(nextUniqueId);
        return nextUniqueId;
    }
}


package server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ObjectState implements Serializable {
    int ID = 0;
    Map<Integer, String> IdName = new HashMap<>();
}

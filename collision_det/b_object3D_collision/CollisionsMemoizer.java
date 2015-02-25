package b_object3D_collision;


import java.util.HashMap;
import java.util.Map;

public class CollisionsMemoizer {
    public Map<Double,Boolean> map;

    CollisionsMemoizer(){
        map = new HashMap<>();
    }

    private double cantorPair(Triangle3D t1, Triangle3D t2){
        int k1 = t1.hashCode();
        int k2 = t2.hashCode();
        if (k1>k2) {
            return 0.5 * (k1 + k2) * (k1 + k2 + 1) + k2;
        }else{
            return 0.5 * (k2 + k1) * (k2 + k1 + 1) + k1;
        }
    }

    public Boolean checkPair(Triangle3D t1,Triangle3D t2){
        double cantoredIndex = cantorPair(t1,t2);
        if (map.containsKey(cantoredIndex)){
            return map.get(cantoredIndex);
        } else{
            return null;
        }
    }

    public void reset(){
        map.clear();
    }

    public void addPair(Triangle3D t1, Triangle3D t2, Boolean isCollision){
        map.put(cantorPair(t1,t2),isCollision);
    }


}

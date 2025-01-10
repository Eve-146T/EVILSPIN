package evil.spin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class WheelDB {
    public List<Wheel> getWheels() {
        return Wheels;
    }

    public void setWheels(List<Wheel> wheels) {
        Wheels = wheels;
    }

    private List<Wheel> Wheels = new ArrayList<>();


    /**
     * @return Takes last element of wheel list and adds 1. Deleted wheels will be ignored, as
     * the amount of wheels to fill is way too large.
     */
    public int GetNewId(){
        if(Wheels.isEmpty()) return 1;
        Wheel lastWheel = Wheels.get(Wheels.size()-1);
        if(Integer.MAX_VALUE-lastWheel.Id==1)
            throw new RuntimeException("Number overflow, you ran out of wheels");
        return lastWheel.Id+1;
    }

    public void AddWheelWithNewId(String name, Collection<String> options)
    {
        AddWheel(new Wheel(GetNewId(),name,options));
    }

    public void AddWheel(Wheel wheelToAdd)
    {
        wheelToAdd.Id=GetNewId();
        Wheels.add(wheelToAdd);
    }
    public Wheel getWheelById(int id) throws Exception {
        // Filter wheels with the matching name
        List<Wheel> matchingWheels = Wheels.parallelStream()
                .filter(wheel -> wheel.Id==id)
                .collect(Collectors.toList());

        // Throw exception if no matching wheels or more than one matching wheel is found
        if (matchingWheels.isEmpty()) {
            return null;
        } else if (matchingWheels.size() > 1) {
            throw new Exception("Multiple wheels found with the id: " + id);
        }

        // Return the single matching wheel
        return matchingWheels.get(0);
    }
    public WheelDBResult RemoveWheel(Wheel wheelToRemove){

            if(getWheels().contains(wheelToRemove)) {
                Wheels.remove(wheelToRemove);
                return WheelDBResult.OK;
            }
            return WheelDBResult.ERROR;
    }
    public boolean IsEmpty(){
        return Wheels.isEmpty();
    }
    public Wheel GetFirst()
    {
        if (Wheels.isEmpty())
            return null;
        return Wheels.get(0);
    }
}

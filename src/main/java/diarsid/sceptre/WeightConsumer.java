/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.sceptre;

/**
 *
 * @author Diarsid
 */
public interface WeightConsumer {
    
    void accept(int index, float weight, WeightElement weightElement);
}

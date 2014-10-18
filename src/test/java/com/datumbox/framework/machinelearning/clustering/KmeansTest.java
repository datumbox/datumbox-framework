/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.machinelearning.clustering;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class KmeansTest {
    
    public KmeansTest() {
    }

    public static Dataset generateDataset() {
        Dataset trainingData = new Dataset();
        
        //Heart Disease - C2: Age, Sex, ChestPain, RestBP, Cholesterol, BloodSugar, ECG, MaxHeartRate, Angina, OldPeak, STSlope, Vessels, Thal
        //http://www.sgi.com/tech/mlc/db/heart.names
        trainingData.add(Record.newDataVector(new Object[] {49,"F","2",134,271,"no","0",162,"no",0,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {42,"M","3",130,180,"no","0",150,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {39,"F","3",94,199,"no","0",179,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {41,"M","2",135,203,"no","0",132,"no",0,2,0,"6"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {56,"M","3",130,256,"yes","2",142,"yes", 0.6,2,1,"6"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {70,"M","2",156,245,"no","2",143,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {56,"M","4",132,184,"no","2",105,"yes", 2.1,2,1,"6"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {63,"F","4",108,269,"no","0",169,"yes", 1.8,2,2,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {64,"M","1",110,211,"no","2",144,"yes", 1.8,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"M","4",110,239,"no","0",126,"yes", 2.8,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {55,"M","4",132,353,"no","0",132,"yes", 1.2,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {48,"M","4",122,222,"no","2",186,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {52,"M","1",152,298,"yes","0",178,"no", 1.2,2,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {46,"M","2",101,197,"yes","0",156,"no",0,1,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {63,"M","4",130,330,"yes","2",132,"yes", 1.8,1,3,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {65,"M","4",135,254,"no","2",127,"no", 2.8,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {42,"M","3",120,240,"yes","0",194,"no", 0.8,3,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {44,"M","4",110,197,"no","2",177,"no",0,1,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {58,"M","4",128,259,"no","2",130,"yes",3,2,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {62,"M","4",120,267,"no","0",99,"yes", 1.8,2,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {58,"M","3",112,230,"no","2",165,"no", 2.5,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {58,"F","3",120,340,"no","0",172,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {60,"M","4",130,206,"no","2",132,"yes", 2.4,2,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {53,"M","3",130,197,"yes","2",152,"no", 1.2,3,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {67,"F","3",115,564,"no","2",160,"no", 1.6,2,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {67,"M","4",100,299,"no","2",125,"yes", 0.9,2,2,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {53,"M","4",123,282,"no","0",95,"yes",2,2,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {59,"M","4",135,234,"no","0",161,"no", 0.5,2,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {58,"M","3",105,240,"no","2",154,"yes", 0.6,2,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {52,"M","4",112,230,"no","0",160,"no",0,1,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {51,"F","3",130,256,"no","2",149,"no", 0.5,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {51,"M","4",140,261,"no","2",186,"yes",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {55,"F","4",180,327,"no","1",117,"yes", 3.4,2,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {59,"F","4",174,249,"no","0",143,"yes",0,2,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {60,"F","3",120,178,"yes","0",96,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {51,"M","3",125,245,"yes","2",166,"no", 2.4,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {68,"M","3",118,277,"no","0",151,"no",1,1,1,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {62,"F","3",130,263,"no","0",97,"no", 1.2,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {50,"M","3",129,196,"no","0",163,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {44,"F","3",108,141,"no","0",175,"no", 0.6,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {51,"F","4",130,305,"no","0",142,"yes", 1.2,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {41,"F","3",112,268,"no","2",172,"yes",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {57,"M","4",150,276,"no","2",112,"yes", 0.6,2,1,"6"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {70,"M","3",160,269,"no","0",112,"yes", 2.9,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {66,"M","4",120,302,"no","2",151,"no", 0.4,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"F","3",160,201,"no","0",163,"no",0,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {67,"F","3",152,277,"no","0",172,"no",0,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {65,"F","3",155,269,"no","0",148,"no", 0.8,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {52,"M","2",128,205,"yes","0",184,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {42,"M","4",136,315,"no","0",125,"yes", 1.8,2,0,"6"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {57,"M","4",110,201,"no","0",126,"yes", 1.5,2,0,"6"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {38,"M","1",120,231,"no","0",182,"yes", 3.8,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {61,"M","4",138,166,"no","2",125,"yes", 3.6,2,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {59,"M","4",140,177,"no","0",162,"yes",0,1,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {42,"M","1",148,244,"no","2",178,"no", 0.8,1,2,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {48,"M","4",130,256,"yes","2",150,"yes",0,1,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {43,"M","4",110,211,"no","0",161,"no",0,1,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {43,"F","3",122,213,"no","0",165,"no", 0.2,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {57,"M","4",140,192,"no","0",148,"no", 0.4,2,0,"6"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {60,"M","4",125,258,"no","2",141,"yes", 2.8,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {35,"F","4",138,183,"no","0",182,"no", 1.4,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"M","4",110,206,"no","2",108,"yes",0,2,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {69,"M","1",160,234,"yes","2",131,"no", 0.1,2,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {57,"M","4",152,274,"no","0",88,"yes", 1.2,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {44,"M","4",112,290,"no","2",153,"no",0,1,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {57,"M","2",124,261,"no","0",141,"no", 0.3,1,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {60,"M","4",117,230,"yes","0",160,"yes", 1.4,1,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {64,"M","4",120,246,"no","2",96,"yes", 2.2,3,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {57,"F","4",120,354,"no","0",163,"yes", 0.6,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {51,"M","3",94,227,"no","0",154,"yes",0,1,1,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"M","3",150,232,"no","2",165,"no", 1.6,1,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {49,"M","2",130,266,"no","0",171,"no", 0.6,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {66,"F","1",150,226,"no","0",114,"no", 2.6,3,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {56,"F","2",140,294,"no","2",153,"no", 1.3,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {43,"M","4",120,177,"no","2",120,"yes", 2.5,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {48,"M","4",124,274,"no","2",166,"no", 0.5,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {60,"F","4",150,258,"no","2",157,"no", 2.6,2,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {59,"M","1",160,273,"no","2",125,"no",0,1,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {50,"F","2",120,244,"no","0",162,"no", 1.1,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {48,"M","2",110,229,"no","0",168,"no",1,3,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {63,"F","3",135,252,"no","2",172,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {62,"M","3",130,231,"no","0",146,"no", 1.8,2,3,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"M","3",120,258,"no","2",147,"no", 0.4,2,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {61,"F","4",145,307,"no","2",146,"yes",1,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {45,"F","4",138,236,"no","2",152,"yes", 0.2,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {56,"M","4",130,283,"yes","2",103,"yes", 1.6,3,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {41,"M","2",110,235,"no","0",153,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"F","3",110,214,"no","0",158,"no", 1.6,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {63,"F","2",140,195,"no","0",179,"no",0,1,2,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {71,"F","4",112,149,"no","0",125,"no", 1.6,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {49,"M","3",120,188,"no","0",139,"no",2,2,3,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {68,"F","3",120,211,"no","2",115,"no", 1.5,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {51,"M","3",110,175,"no","0",123,"no", 0.6,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"M","4",120,188,"no","0",113,"no", 1.4,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {62,"F","4",124,209,"no","0",163,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {61,"M","1",134,234,"no","0",145,"no", 2.6,2,2,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {43,"M","4",150,247,"no","0",171,"no", 1.5,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {50,"M","4",150,243,"no","2",128,"no", 2.6,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {56,"M","2",120,236,"no","0",178,"no", 0.8,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {57,"M","3",150,168,"no","0",174,"no", 1.6,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {49,"F","4",130,269,"no","0",163,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {52,"M","4",128,255,"no","0",161,"yes",0,1,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {46,"M","3",150,231,"no","0",147,"no", 3.6,2,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {62,"F","4",140,268,"no","2",160,"no", 3.6,3,2,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {47,"M","4",112,204,"no","0",143,"no", 0.1,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {65,"M","4",110,248,"no","2",158,"no", 0.6,1,2,"6"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {48,"M","3",124,255,"yes","0",175,"no",0,1,2,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {62,"F","4",150,244,"no","0",154,"yes", 1.4,2,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {53,"M","4",142,226,"no","2",111,"yes",0,1,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {62,"M","2",128,208,"yes","2",140,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {66,"M","4",112,212,"no","2",132,"yes", 0.1,1,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {44,"M","2",120,263,"no","0",173,"no",0,1,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {71,"F","3",110,265,"yes","2",130,"no",0,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {45,"M","4",115,260,"no","2",185,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {64,"F","4",180,325,"no","0",154,"yes",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {58,"F","1",150,283,"yes","2",162,"no",1,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {52,"M","2",134,201,"no","0",158,"no", 0.8,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {59,"M","2",140,221,"no","0",164,"yes",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {43,"M","3",130,315,"no","0",162,"no", 1.9,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {65,"F","3",140,417,"yes","2",157,"no", 0.8,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"M","2",108,309,"no","0",156,"no",0,1,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {42,"M","2",120,295,"no","0",162,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {46,"F","2",105,204,"no","0",172,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {60,"M","3",140,185,"no","2",155,"no",3,2,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {60,"F","1",150,240,"no","0",171,"no", 0.9,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"F","3",108,267,"no","2",167,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {59,"M","4",110,239,"no","2",142,"yes", 1.2,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {54,"M","4",122,286,"no","2",116,"yes", 3.2,2,2,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {50,"F","4",110,254,"no","2",159,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {41,"M","3",112,250,"no","0",179,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {44,"M","2",120,220,"no","0",170,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {61,"F","4",130,330,"no","2",169,"no",0,1,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {47,"M","4",110,275,"no","2",118,"yes",1,2,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {40,"M","4",152,223,"no","0",181,"no",0,1,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {66,"F","4",178,228,"yes","0",165,"yes",1,2,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {62,"F","4",138,294,"yes","0",106,"no", 1.9,2,3,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {57,"M","3",128,229,"no","2",150,"no", 0.4,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {58,"M","2",120,284,"no","2",160,"no", 1.8,2,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {41,"F","2",105,198,"no","0",168,"no",0,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {60,"F","4",158,305,"no","2",161,"no",0,1,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {51,"M","4",140,299,"no","0",173,"yes", 1.6,1,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {42,"F","3",120,209,"no","0",173,"no",0,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {29,"M","2",130,204,"no","2",202,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {69,"F","1",140,239,"no","0",151,"no", 1.8,1,2,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {65,"M","1",138,282,"yes","2",174,"no", 1.4,2,1,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {58,"M","4",150,270,"no","2",111,"yes", 0.8,1,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {54,"M","3",125,273,"no","2",152,"no", 0.5,3,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {64,"F","4",130,303,"no","0",122,"no",2,2,2,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {53,"M","4",140,203,"yes","2",155,"yes", 3.1,3,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {58,"M","3",132,224,"no","2",173,"no", 3.2,1,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {61,"M","4",120,260,"no","0",140,"yes", 3.6,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {64,"M","3",125,309,"no","0",131,"yes", 1.8,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {53,"F","4",130,264,"no","2",143,"no", 0.4,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {52,"F","3",136,196,"no","2",169,"no", 0.1,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {62,"M","2",120,281,"no","2",103,"no", 1.4,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {70,"M","4",145,174,"no","0",125,"yes", 2.6,3,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {52,"M","4",125,212,"no","0",168,"no",1,1,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {58,"F","2",136,319,"yes","2",152,"no",0,1,2,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {35,"M","4",120,198,"no","0",130,"yes", 1.6,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {58,"M","3",140,211,"yes","2",165,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {67,"M","4",125,254,"yes","0",163,"no", 0.2,2,2,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {76,"F","3",140,197,"no","1",116,"no", 1.1,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {59,"M","3",150,212,"yes","0",157,"no", 1.6,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {47,"M","3",108,243,"no","0",152,"no",0,1,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {59,"M","1",178,270,"no","2",145,"no", 4.2,3,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {35,"M","4",126,282,"no","2",156,"yes",0,1,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {50,"M","4",144,200,"no","2",126,"yes", 0.9,2,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {50,"M","3",140,233,"no","0",163,"no", 0.6,2,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {63,"M","1",145,233,"yes","2",150,"no", 2.3,3,0,"6"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {46,"F","4",138,243,"no","2",152,"yes",0,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {54,"F","3",135,304,"yes","0",170,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {44,"M","3",140,235,"no","2",180,"no",0,1,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {67,"M","4",120,237,"no","0",71,"no",1,2,0,"3"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {74,"F","2",120,269,"no","2",121,"yes", 0.2,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {64,"F","3",140,313,"no","0",133,"no", 0.2,1,0,"7"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {60,"F","3",102,318,"no","0",160,"no",0,1,1,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {46,"M","4",120,249,"no","2",144,"no", 0.8,1,0,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {61,"M","4",140,207,"no","2",138,"yes", 1.9,1,1,"7"}, "problem"));
        trainingData.add(Record.newDataVector(new Object[] {51,"M","3",100,222,"no","0",143,"yes", 1.2,2,0,"3"}, "healthy"));
        trainingData.add(Record.newDataVector(new Object[] {67,"M","4",120,229,"no","2",129,"yes", 2.6,2,2,"7"}, "problem"));
        
        return trainingData;
    }

    /**
     * Test of predict method, of class MaximumEntropy.
     */
    @Test
    public void testPredict() {
        System.out.println("predict");
        RandomValue.randomGenerator = new Random(42); 
        
        Dataset trainingData = generateDataset();
        Dataset validationData = new Dataset();
        validationData.add(Record.newDataVector(new Object[] {51,"M","3",100,222,"no","0",143,"yes", 1.2,2,0,"3"}, "healthy"));
        validationData.add(Record.newDataVector(new Object[] {67,"M","4",120,229,"no","2",129,"yes", 2.6,2,2,"7"}, "problem"));
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        
        String dbName = "JUnitClusterer";
        

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName);
        df.initializeTrainingConfiguration(memoryConfiguration, df.getEmptyTrainingParametersObject());
        df.transform(trainingData, true);
        df.normalize(trainingData);
        df.transform(validationData, false);
        df.normalize(validationData);
        
        Kmeans instance = new Kmeans(dbName);
        
        Kmeans.TrainingParameters param = instance.getEmptyTrainingParametersObject();
        param.setK(2);
        param.setMaxIterations(200);
        param.setInitMethod(Kmeans.TrainingParameters.Initialization.FORGY);
        param.setDistanceMethod(Kmeans.TrainingParameters.Distance.EUCLIDIAN);
        param.setWeighted(false);
        param.setCategoricalGamaMultiplier(1.0);
        param.setSubsetFurthestFirstcValue(2.0);
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        instance.train(trainingData, validationData);
        
        
        instance = null;
        instance = new Kmeans(dbName);
        instance.setMemoryConfiguration(memoryConfiguration);
        instance.predict(validationData);
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        df.erase(true);
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        
        Map<Integer, Kmeans.Cluster> clusters = instance.getClusters();
        for(Record r : validationData) {
            expResult.put(r.getId(), r.getY());
            Integer clusterId = (Integer) r.getYPredicted();
            Object label = clusters.get(clusterId).getLabelY();
            if(label==null) {
                label = clusterId;
            }
            result.put(r.getId(), label);
        }
        assertEquals(expResult, result);
        
        instance.erase(true);
    }

    
    /**
     * Test of kFoldCrossValidation method, of class Kmeans.
     */
    @Test
    public void testKFoldCrossValidation() {
        System.out.println("kFoldCrossValidation");
        RandomValue.randomGenerator = new Random(42); 
        int k = 5;
        
        Dataset trainingData = generateDataset();
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        
        
        String dbName = "JUnitRegressor";

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName);
        df.initializeTrainingConfiguration(memoryConfiguration, df.getEmptyTrainingParametersObject());
        df.transform(trainingData, true);
        df.normalize(trainingData);

        
        
        
        Kmeans instance = new Kmeans(dbName);
        
        Kmeans.TrainingParameters param = instance.getEmptyTrainingParametersObject();
        param.setK(2);
        param.setMaxIterations(200);
        param.setInitMethod(Kmeans.TrainingParameters.Initialization.FORGY);
        param.setDistanceMethod(Kmeans.TrainingParameters.Distance.EUCLIDIAN); 
        param.setWeighted(false);
        param.setCategoricalGamaMultiplier(1.0);
        param.setSubsetFurthestFirstcValue(2.0);
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        Kmeans.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, k);

        df.denormalize(trainingData);
        df.erase(true);

        
        double expResult = 0.7888888888888889;
        double result = vm.getPurity();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_MEDIUM);
        instance.erase(true);
    }

    
}

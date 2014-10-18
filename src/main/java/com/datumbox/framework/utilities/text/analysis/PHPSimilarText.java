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
package com.datumbox.framework.utilities.text.analysis;

/**
 * I ported from php's similar_text() method which is written in C and it's
 * original code can be found on:
 * http://stackoverflow.com/questions/14136349/how-does-similar-text-work
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class PHPSimilarText {
    
    private static class Tuple {
        private int pos1=0;
        private int pos2=0;
        private int max=0;

        public int getPos1() {
            return pos1;
        }

        public void setPos1(int pos1) {
            this.pos1 = pos1;
        }

        public int getPos2() {
            return pos2;
        }

        public void setPos2(int pos2) {
            this.pos2 = pos2;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }
    }
    
    public static int similarityChars(String txt1, String txt2) {
        int sim = similar_char(txt1, txt1.length(), txt2, txt2.length());
        return sim;
    }
    
    public static double similarityPercentage(String txt1, String txt2) {
        double sim = similarityChars(txt1, txt2);
        return sim * 200.0 / (txt1.length() + txt2.length());
    }

    private static void similar_str(String txt1, int len1, String txt2, int len2, Tuple t) {
        t.setMax(0);
        for (int p = 0; p < len1; ++p) {
            for (int q = 0; q < len2; ++q) {
                int l;
                for (l = 0; (p+l < len1) && (q+l < len2) && (txt1.charAt(p+l) == txt2.charAt(q+l)); ++l) {
                    
                }
                if (l > t.getMax()) {
                    t.setMax(l);
                    t.setPos1(p);
                    t.setPos2(q);
                }
            }
        }
    }

    private static int similar_char(String txt1, int len1, String txt2, int len2) {
        int sum;

        Tuple t = new Tuple();

        similar_str(txt1, len1, txt2, len2, t);

        if ((sum = t.getMax()) != 0) {
            if (t.getPos1() != 0 && t.getPos2() != 0) {
                sum += similar_char(txt1, t.getPos1(), txt2, t.getPos2());
            }
            if ((t.getPos1() + t.getMax() < len1) && (t.getPos2() + t.getMax() < len2)) {
                sum += similar_char(txt1.substring(t.getPos1()+t.getMax()), len1 - t.getPos1() - t.getMax(), txt2.substring(t.getPos2()+t.getMax()), len2 - t.getPos2() - t.getMax());
            }
        }

        return sum;
    }
}
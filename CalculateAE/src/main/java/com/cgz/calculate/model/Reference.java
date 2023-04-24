package com.cgz.calculate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 该类重写了equals和hashCode方法：
 * 如果两个对象的from相等且to相等或该对象的from等于另一对象的to且该对象的to等于另一对象的from，即认为这两个对象相等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reference {
    private String from;
    private String to;
    private int refNum;
    private int fromEntityNum;
    private int toEntityNum;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reference reference = (Reference) o;
        return Objects.equals(from, reference.from) && Objects.equals(to, reference.to)
                ||Objects.equals(from, reference.to) && Objects.equals(to, reference.from);
    }

    @Override
    public int hashCode() {
        String min;
        String max;
        if(from.compareTo(to)<0){
            min = from;
            max = to;
        }else {
            min = to;
            max = from;
        }
        return Objects.hash(min+max);
    }
}

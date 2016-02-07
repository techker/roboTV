package org.xvdr.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class AdaptiveAllocator {

    final static String TAG = "AdaptiveAllocator";

    private List<Allocation> m_list;


    public AdaptiveAllocator(int initialBufferCount, int initalBufferSize) {
        m_list = new ArrayList<>(initialBufferCount);

        for(int i = 0; i < initialBufferCount; i++) {
            m_list.add(new Allocation(initalBufferSize));
        }
    }

    synchronized public Allocation allocate(int neededSize) {
        // check for a suitable packet
        Allocation p = findAllocation(neededSize);

        if(p != null) {
            return p;
        }

        // resize an unallocated packet
        p = findUnallocated();

        if(p != null) {
            p.resize(neededSize);
            sort();
            return p;
        }

        // add a new packet
        p = new Allocation(neededSize);
        m_list.add(p);

        p.allocate();
        sort();

        return p;
    }

    synchronized public void release(Allocation a) {
        a.release();
    }

    private Allocation findAllocation(int neededSize) {
        for(Allocation p : m_list) {
            if(p.size() >= neededSize && p.allocate()) {
                return p;
            }
        }

        return null;
    }

    private Allocation findUnallocated() {
        ListIterator<Allocation> li = m_list.listIterator(m_list.size());

        while(li.hasPrevious()) {
            Allocation p = li.previous();

            if(p.allocate()) {
                return p;
            }
        }

        return null;
    }

    private void sort() {
        Collections.sort(m_list, new Comparator<Allocation>() {
            @Override
            public int compare(Allocation lhs, Allocation rhs) {
                return lhs.size() < rhs.size() ? -1 : 1;
            }
        });
    }
}
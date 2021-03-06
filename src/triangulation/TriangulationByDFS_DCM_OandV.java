package triangulation;

import util.Counter;
import graph.BronKerboschAlgorithm;
import graph.Graph;
import util.GraphUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import static util.GraphUtil.eliminateSimplicial;

/**
 * Created by chaoli on 10/27/16.
 */
public class TriangulationByDFS_DCM_OandV extends TriangulationByDFS {

    protected void expandNode(Graph n_H, BitSet n_remaining, List<BitSet> n_cliques, BigInteger n_tts, int[] weights) {

        elapsed_time_ =         System.nanoTime() - start_time_;
        if(elapsed_time_ / 1000000000L > 3600L){
            System.out.println("time out");
            return;
        }


        //  The number of nodes + 1
        nodeCounter_.increment();

        /*branch & bound*/
        for (int v = n_remaining.nextSetBit(0); v >= 0; v = n_remaining.nextSetBit(v + 1)) {

            /*Let m = Copy(n)*/
            Graph m_H = new Graph(n_H);
            BitSet m_remaining = (BitSet) n_remaining.clone();
            List<BitSet> m_cliques = new ArrayList<>(n_cliques);
            BigInteger m_tts = n_tts;

            /* EliminateVertex(m, v)*/
            long start_time = System.nanoTime();

            m_tts = eliminateVertex_DCM_OandV(m_H, m_remaining, v, m_cliques, m_tts, weights, cliqueCounter_);

            //  long start_time = System.nanoTime();
            long end_time = System.nanoTime();
            time_for_DCM_   += (end_time - start_time);

            eliminateSimplicial(m_H, m_remaining);  //  GraphUtil.eliminateSimplicial

            if (m_remaining.isEmpty()) {
                if (m_tts.compareTo(best_tts_) < 0) {   //  Found a new goal node
                    best_H_ = m_H;
                    best_tts_ = m_tts;
                }
            } else {
                //  greater than upper bound
                if (m_tts.compareTo(best_tts_) >= 0) {
                    continue;
                }
                //  map pruning
                if (map.get(m_remaining) != null && map.get(m_remaining).compareTo(m_tts) <= 0) {
                    continue;
                }

                map.put(m_remaining, m_tts);
                expandNode(m_H, m_remaining, m_cliques, m_tts, weights);
            }
        }
    }

    public static BigInteger eliminateVertex_DCM_OandV(Graph graph, BitSet remaining, int v, List<BitSet> m_cliques, BigInteger m_tts, int[] weights, Counter cliqueCounter_) {
        BitSet U = new BitSet(graph.V());
        BitSet fa_U_G = new BitSet(graph.V());

        BitSet nb = util.BitSetUtil.set_intersection(graph.getNeighbors(v), remaining);
        for (int v1 = nb.nextSetBit(0); v1 >= 0; v1 = nb.nextSetBit(v1 + 1)) {
            for (int v2 = nb.nextSetBit(v1 + 1); v2 >= 0; v2 = nb.nextSetBit(v2 + 1)) {
                if (!graph.containsEdge(v1, v2)) {
                    graph.addEdge(v1, v2);
                    U.set(v1);
                    U.set(v2);
                }
            }
        }

        for (int w = nb.nextSetBit(0); w >= 0; w = nb.nextSetBit(w + 1)) {
            fa_U_G.or(graph.getNeighbors(w));
        }
        fa_U_G.or(U);

        //Remove old cliques
        Iterator<BitSet> it = m_cliques.iterator();
        while (it.hasNext()) {
            BitSet clique = it.next();
            if (U.intersects(clique)) {
                m_tts = m_tts.subtract(util.BitSetUtil.tableSize(clique, weights));
                it.remove();
            } else {
                //System.out.println(clique);
            }
        }

        // Add new cliques
        List<BitSet> newCliques = new BronKerboschAlgorithm(graph, fa_U_G).getAllMaximalCliques();
        it = newCliques.iterator();
        while (it.hasNext()) {
            cliqueCounter_.increment();
            BitSet clique = it.next();
            if (U.intersects(clique)) {
                if (GraphUtil.isClique(clique, graph)) {
                    m_tts = m_tts.add(util.BitSetUtil.tableSize(clique, weights));
                    m_cliques.add(clique);
                }
            }
        }

        remaining.clear(v);

        return m_tts;
    }
}
latex input:    mmd-article-header
Title:          Data Structures Notes
Author:         Ethan C. Petuchowski
Base Header Level:  1
latex mode:     memoir
Keywords:       Data Structures, Algorithms
CSS:            http://fletcherpenney.net/css/document.css
xhtml header:   <script type="text/javascript" src="http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML">
</script>
copyright:      2014 Ethan C. Petuchowski
latex input:    mmd-natbib-plain
latex input:    mmd-article-begin-doc
latex footer:   mmd-memoir-footer


## Tree

* **Perfect** -- every level is full

               x
             /   \
            /     \
           x       x
          / \     / \
         x   x   x   x
        / \ / \ / \ / \
        x x x x x x x x

* **Complete** -- every level, except possibly the last,
                  is completely filled, and all nodes are
                  as far left as possible

               x
             /   \
            /     \
           x       x
          / \     / \
         x   x   x   x
        / \ /
        x x x

* **Full** -- every node has either two children or zero children

               x
             /   \
            /     \
           x       x
          / \     / \
         x   x   x   x
        / \ / \
        x x x x

* **Height** -- *distance* from root to deepest leaf
    * So for the above tree examples, the *height* is **3** (*not* 4)


### Heap

1. [Wikipedia](http://en.wikipedia.org/wiki/Heap_(data_structure))
2. [Algorithms -- Sedgewick, pg. 316]()
3. [Heapsort Summary Page](http://www.sorting-algorithms.com/heap-sort)

#### 5/4/14

* a binary heap is a **complete** binary tree which satisfies the heap
  ordering property.
* The ordering can be one of two types:
    1. the **min-heap** property: the value of each node is *greater than or
       equal* to the value of its parent, with the minimum-value element at
       the root.
    2. the **max-heap** property: same but flipped
* A heap is not a sorted structure but can be regarded as **partially
  ordered**.
    * There is no particular relationship among nodes on any given level, even
      among the siblings
* The heap is one maximally efficient implementation of a **priority queue**

#### 6/27/14

* the parent of the node at position `k` in a heap is at position `k/2`
* See implementation with explanation at
  `~/Dropbox/CSyStuff/PrivateCode/PreDraft_6-27-14`


### Deque -- double-ended queue

**Elements can be added/removed/inspected from either the front or the back.**

#### Implementations

* **Doubly-linked-list** -- all required operations are O(1), random access O(n)
* **Growing array** -- *amortized* time is O(1), random access O(1)

##### Java

1. `LinkedList<T>` -- doubly-linked-list
2. `ArrayDeque<T>` -- growing array


## Bloom Filters

[i-programmer](http://www.i-programmer.info/programming/theory/2404-the-bloom-filter.html)

### The Point

* **Bloom filters *attempt* to tell you if you have seen a particular data
  item before**
* **False-positives** are ***possible***, **false-negatives** are ***not***

#### Approximate answers are faster

* You can usually trade space for time; the more storage you can throw at a
  problem the faster you can make it run.
* In general you can also trade certainty for time.

### Applications

* *Google's BigTable database* uses one to reduce lookups for data rows that
  haven't been stored.
* The *Squid proxy server* uses one to avoid looking up things that aren't in
  the cache and so on

### History

* Invented in 1970 by Burton *Bloom*

### How it works

* Uses multiple different hash functions in-concert

#### Initialization

* A Bloom filter starts off with a **bit array** `Bloom[i]` initialized to
  zero.

#### Insertion

* To record each data value you simply compute *k* different hash functions
* Treat the resulting *k* values as indices into the array and set each of the
  *k* array elements to `1`.

#### Lookup

* Given an item to look up, apply the *k* hash functions and look up the
  indicated array elements.
* If any of them are zero you can be 100% sure that you have never encountered
  the item before.
* *However* even if all of them are one then you can't conclude that you have
  seen the data item before.
    * All you can conclude is that it is *likely* that you have encountered
      the data item before.

#### Removal

* **It is impossible to remove an item from a Bloom filter.**


### Characteristics

* As the bit array fills up the probability of a false positive increases
* There is a formula for calculating the optimal number of hash functions to
  use for a given size of the bit array and the number of items you plan to
  store in there
    * k_opt = 0.7(m/n)
* There is another formula for calculating the size to use for a given desired
  probability of error and fixed number of items using the optimal number of
  hash functions from above
    * m = -2n * ln(p)


## Red Black Tree

[Tim Roughgarden's Coursera lecture on it](https://www.youtube.com/watch?v=4slgC3UOXc0)

* A form of *balanced* binary search tree with additional imposed
  **invariants** to ensure that all the common operations (insert, delete,
  min, max, pred, succ, search) happen in O(log(n)) time.

#### Invariants

1. Each node is red or black
2. Root is black
3. A red node must have black children
4. Every `root->NULL` path through the tree has same number of black nodes

Theorem: every red-black tree with *n* nodes has height ≤ 2log_2(n+1)

#### Implementations

This is what backs Java's `TreeMap<T>`

## SkipList

* Allows fast search within an ordered sequence of elements
* "Skip list algorithms have the same asymptotic expected time bounds as
  balanced trees and are simpler, faster and use less space." --- inventor
  William Pugh
    * \\(log(n)\\) for contains, insert, and remove
* a "data structure for storing a sorted list of items, using a hierarchy of
  linked lists that connect increasingly sparse subsequences of the items."
* It's hard to explain but the picture on Wikipedia makes it clear
* So you walk down the highest (sparsest) list until you find that you've
  skipped your element
    * If you found your element, you're good to go
* Then you go to the most recent element before you skipped over the element,
  and walk down a lower list
* The last list contains your entire sequence and you'll definitely find your
  element there

## Distributed Hash Table (DHT)

1. Same interface as a *hash table* (look up *value* by *key*)
2. Responsibility for maintaining the mapping \\(keys\rightarrow values\\) is
   *distributed* among the *nodes*
3. We require change in who is participating to cause minimal disruption
4. Useful for web caching, distributed file systems, DNS, IM, multicast, P2P
   (e.g. BitTorrent's distributed "tracker"), content distribution, and search
   engines
5. Properties
    1. decentralization/autonomy --- no central coordinator
    2. fault-tolerance --- nodes can continuously join, leave, or fail
    3. scalability --- still functions efficiently with millions of nodes
6. Generally each node must coordinate with \\(O(\mathrm{log} n)\\) other nodes
7. Can be optionally designed for better security against malicious
   participants, and to allow participants to remain anonymous
8. Handles load balancing, data integrity, and performance

### Structure

1. We start with a *keyspace* and a defined *partitioning scheme*
2. To add a new entry
    1. Hash it
    2. Send it to *any* participating node
    3. Keep *forwarding* it until the *single* responsible node is reached
    4. The responsible adds the entry
3. Getting an entry is quite similar
4. Uses **consistent hashing** --- has property that when the table is
   resized, only \\(\frac{#keys}{#slots}\\) keys must be remapped.
    1. The hashing techniques make it so that only those members adjacent in
       the keyspace to a new node have to have they're data sloshed around
5. The **Overlay network** is the set of links connecting nodes
    1. Requires the property that for any key, each node either owns it, or
       has a link to someone "closer" to it in terms of some defined keyspace
       distance
    1. There's a tradeoff between the number of links we require each node to
       have ("degree") and the "route length" queries require

# TODO

Scala's `Vector` is a tree of arrays, kind of like a *B-Tree*, also like one
of the main *file formats*


#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools


@tools.exectime
def heap_sort(array):
    def heap_adjust(parent):
        child = 2 * parent + 1  # left child
        while child < len(heap):
            if child + 1 < len(heap):
                if heap[child + 1] > heap[child]:
                    child += 1  # right child
            if heap[parent] >= heap[child]:
                break
            heap[parent], heap[child] = \
                heap[child], heap[parent]
            parent, child = child, 2 * child + 1

    heap, array = array.copy(), []
    for i in range(len(heap) // 2, -1, -1):
        heap_adjust(i)
    while len(heap) != 0:
        heap[0], heap[-1] = heap[-1], heap[0]
        array.insert(0, heap.pop())
        heap_adjust(0)
    return array


def main():
    """堆排序

    （1）将初始待排序关键字序列(R1,R2….Rn)构建成大顶堆，此堆为初始的无序区；
    （2）将堆顶元素R[1]与最后一个元素R[n]交换，此时得到新的无序区(R1,R2,……Rn-1)和新的有序区(Rn),且满足R[1,2…n-1]<=R[n]；
    （3）由于交换后新的堆顶R[1]可能违反堆的性质，因此需要对当前无序区(R1,R2,……Rn-1)调整为新堆，
        然后再次将R[1]与无序区最后一个元素交换，得到新的无序区(R1,R2….Rn-2)和新的有序区(Rn-1,Rn)。
        不断重复此过程直到有序区的元素个数为n-1，则整个排序过程完成。

    :return:
    """

    array = tools.load_random_array()
    print heap_sort(array) == sorted(array)


if __name__ == '__main__':
    main()

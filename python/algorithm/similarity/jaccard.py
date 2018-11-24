#!/usr/bin/env python
# -*- coding:utf-8 -*-


def jaccard_distance(prefs, p1, p2):
    si = {}
    for item in prefs[p1]:
        if item in prefs[p2]:
            si[item] = 1
    n = len(si)
    if n == 0:
        return 0
    return n / (len(prefs[p1]) + len(prefs[p2]) - n)


def main():
    print 'test'


if __name__ == '__main__':
    main()

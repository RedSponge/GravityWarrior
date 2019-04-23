import os

names = [
        ("idle", 4),
        ("run", 8),
        ("hit", 2),
        ("fallen", 1),
        ("head_stuck", 2),
        ("plunging", 1),
        ("slice", 7),
        ("duck", 2)
]

offset = 1
for name in names:
    for i in range(name[1]):
        os.rename("enemy%d.png" % (offset + i), "%s_%d.png" % (name[0], i + 1))
    offset += name[1]


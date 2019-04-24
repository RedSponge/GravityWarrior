import os

names = [
        ("idle", 4),
        ("run", 8),
        ("hit", 2),
        ("fallen", 1),
        ("head_stuck", 2),
        ("plunging", 1),
        ("slice", 7),
        ("duck", 3)
]

offset = 1
for name in names:
    for i in range(name[1]):
        old_name = "enemy%d.png" % (offset + i)
        new_name = "%s_%d.png" % (name[0], i + 1)
        exists = os.path.isfile(new_name)
        if exists:
            os.remove(new_name)
        os.rename(old_name, new_name)
    offset += name[1]


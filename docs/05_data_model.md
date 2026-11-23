# Модель данных

СУБД — Room поверх SQLite. Две сущности со связью один ко многим. Модель зафиксирована
на первом спринте, чтобы экраны и расчёты проектировались под реальные данные.

## 1. Сущности

### Discipline

| Поле | Тип Kotlin | Ограничение | Назначение |
|---|---|---|---|
| `id` | `Long` | PK, autoGenerate | идентификатор |
| `name` | `String` | NOT NULL, непустое | название предмета |
| `teacherName` | `String?` | NULL | преподаватель |

### Grade

| Поле | Тип Kotlin | Ограничение | Назначение |
|---|---|---|---|
| `id` | `Long` | PK, autoGenerate | идентификатор |
| `disciplineId` | `Long` | FK → `Discipline.id`, ON DELETE CASCADE, индекс | принадлежность оценке |
| `value` | `Int` | CHECK 2…5 | значение оценки |
| `date` | `LocalDate` | NOT NULL | дата выставления |
| `workType` | `WorkType` | NOT NULL, enum в строку | вид работы |
| `comment` | `String?` | NULL, до 300 символов | комментарий |

`minSdk 26` выбран в том числе потому, что `java.time.LocalDate` доступен без desugaring.

## 2. Связь и удаление

```
Discipline 1 ──── N Grade
```

Удаление дисциплины обрабатывается осознанно: интерфейс показывает подтверждение с
количеством оценок, после подтверждения записи удаляются каскадно. Альтернатива
«запретить удаление при наличии оценок» отклонена: студент должен уметь убрать ошибочно
созданный предмет, а случайное удаление защищено диалогом.

## 3. SQL-эскиз

Room сгенерирует схему из Entity; эскиз нужен для проверки на бумаге.

```sql
CREATE TABLE discipline (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    teacherName TEXT
);

CREATE TABLE grade (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    disciplineId INTEGER NOT NULL REFERENCES discipline(id) ON DELETE CASCADE,
    value        INTEGER NOT NULL CHECK (value BETWEEN 2 AND 5),
    date         TEXT    NOT NULL,   -- ISO-8601, yyyy-MM-dd
    workType     TEXT    NOT NULL,
    comment      TEXT
);

CREATE INDEX idx_grade_discipline ON grade(disciplineId);
```

## 4. Виды работ

Хранятся строкой enum, русские подписи — в ресурсах:

| Константа | Подпись |
|---|---|
| `CONTROL` | Контрольная работа |
| `PRACTICE` | Практическая работа |
| `LAB` | Лабораторная работа |
| `ANSWER` | Устный ответ |
| `TEST` | Тест |
| `OTHER` | Другое |

## 5. Реализация (спринт 2)

Модель реализована: `DisciplineEntity`, `GradeEntity`, `DisciplineDao`, `GradeDao`,
`GradebookDatabase` (версия 1, схема экспортируется в `app/schemas/`), конвертеры
`LocalDate ↔ epochDay` и `WorkType ↔ String`. Демо-набор — 5 дисциплин и 25 оценок,
сумма 106, средний балл 4,24. Подробности и результаты проверки — в
[07_storage.md](07_storage.md).

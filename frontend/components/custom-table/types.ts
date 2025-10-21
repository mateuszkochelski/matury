export type FieldOfStudy = {
  id: number;
  name: string;
  level: string;
  duration: number;
  language: string;
  university: {
    id: number;
    name: string;
    acronym: string;
    city: string;
  };
  department: {
    id: number;
    name: string;
  };
};

export type FieldOfStudyData = {
  content: FieldOfStudy[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

export const possibleDegrees = [
  "bachelors",
  "engineering",
  "masters",
  "engineeringMasters",
] as const;

export type DegreeType = (typeof possibleDegrees)[number];

export type DegreesObject = {
  [K in DegreeType]: boolean;
};

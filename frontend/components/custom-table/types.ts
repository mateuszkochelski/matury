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

export type FieldOfStudyExtended = {
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
  passRate: number;
  avgIncome: number;
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

export type FieldOfStudyExtendedData = {
  content: FieldOfStudyExtended[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

export const possibleDegrees = ["bachelor", "engineer", "master", "long_master"] as const;

export type DegreeType = (typeof possibleDegrees)[number];

export type DegreesObject = {
  [K in DegreeType]: boolean;
};

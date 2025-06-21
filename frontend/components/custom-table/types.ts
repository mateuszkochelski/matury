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

import { BACKEND_URL } from "../constants";

type FieldOfStudy = {
  id: number,
  name: string,
  level: string;
  duration: number;
  language: string;
  university: {
    id: number;
    name: string;
    acronym: string;
    city: string;
  }
  department: {
    id: number;
    name: string;
  }
}

type FieldOfStudyData = {
  content: FieldOfStudy[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number
  }
}

export default async function Home() {
  // an example on how to fetch on server
  const response = await fetch(`${BACKEND_URL}/api/field_of_study`);
  const data: FieldOfStudyData = await response.json();
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const {content: fields, page: pageData} = data;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const {number: pageNumber} = pageData;

  return (
    <div>
      <span>Check the console for data</span>
    </div>
  );
}

import { Button } from "@/components/ui/button";
import { CustomBreadcrumb } from "@/components/ui/custom-breadcrumb/CustomBradcrumb";
import DepartmentCards from "@/components/ui/department-cards/DepartmentCards";
import UniversityData from "@/components/ui/university-data/UniversityData";
import { getUniversityData } from "@/utils/getUniversityData";
import Link from "next/link";
import { notFound } from "next/navigation";

export default async function Home({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  if (!id) {
    notFound();
  }

  const { universityData, departmentData } = await getUniversityData(id);
  const departments = departmentData.content;

  return (
    <main className="min-h-screen flex flex-col p-8 pb-20 gap-4 sm:gap-4 sm:px-20 md:px-40 lg:px-70 font-[family-name:var(--font-geist-sans)]">
      <CustomBreadcrumb
        items={[{ name: "Strona główna", href: "/" }, { name: universityData.name }]}
      />
      <h1>{universityData.name}</h1>
      <div className="w-full min-h-96 border border-black">Mapa</div>
      <h2>Dane podstawowe</h2>
      <UniversityData
        data={[
          { name: "Akronim", value: universityData.acronym },
          { name: "Liczba wydziałów", value: departments.length.toString() },
          { name: "Adres", value: universityData.address },
        ]}
      />
      {universityData.url && universityData.url != "" ? (
        <Link href={universityData.url}>
          <Button className="bg-blue-200 hover:bg-blue-300 text-black font-normal w-full p-5 border border-black cursor-pointer">
            Strona uczelni
          </Button>
        </Link>
      ) : null}
      <h3>O uczelni</h3>
      <p>{universityData.description}</p>
      <h3>Wydziały na tej uczelni</h3>
      <DepartmentCards departments={departments}></DepartmentCards>
    </main>
  );
}

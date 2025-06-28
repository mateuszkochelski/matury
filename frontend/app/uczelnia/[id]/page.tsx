import { CustomBreadcrumb } from "@/components/custom-breadcrumb/CustomBradcrumb";
import DepartmentCards from "@/components/department-cards/DepartmentCards";
import Map from "@/components/google-map/GoogleMap";
import { Button } from "@/components/ui/button";
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
    <main className="min-h-screen flex flex-col p-2 pb-20 gap-4 sm:gap-4 sm:p-8 mx-auto max-w-[900px] font-[family-name:var(--font-geist-sans)]">
      <CustomBreadcrumb
        items={[{ name: "Strona główna", href: "/" }, { name: universityData.name }]}
      />
      <h1>{universityData.name}</h1>
      <Map query={universityData.name}></Map>
      {universityData.url && universityData.url != "" ? (
        <Link href={universityData.url}>
          <Button className="bg-blue-200 hover:bg-blue-300 text-black font-normal w-full p-5 border border-black cursor-pointer">
            Strona uczelni
          </Button>
        </Link>
      ) : null}
      <h2>O uczelni</h2>
      <p>{universityData.description}</p>
      <h2>Wydziały na tej uczelni</h2>
      <DepartmentCards departments={departments}></DepartmentCards>
    </main>
  );
}
